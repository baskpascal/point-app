package point.app.session

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

/**
 * DEV-ONLY transport for the Gemini Live loop. Talks the BidiGenerateContent
 * websocket protocol directly, keyed by [GeminiDevKey]. This exists only to
 * unblock local audio + live-vision validation while the Firebase project's
 * Gemini access is sorted out; production swaps back to [FirebaseAiLiveSession]
 * behind this same [AiLiveSession] interface. Do not build app structure on it.
 */
internal class DirectGeminiLiveSession(
    appContext: Context,
    private val apiKey: String,
) : AiLiveSession {

    private companion object {
        // gemini-3.1-flash-live: ~0.7s to first audio vs ~3.4s for the 2.5 native-audio
        // preview (measured), and it isn't rate-limited to death on this key.
        const val MODEL = "models/gemini-3.1-flash-live-preview"
        const val WS_URL =
            "wss://generativelanguage.googleapis.com/ws/" +
                "google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
        const val CAPTURE_MIME = "audio/pcm;rate=16000"
        // Client-side VAD thresholds on the scaled RMS (rms()* returns ~0..1).
        const val VAD_START = 0.16f
        const val VAD_END = 0.10f
        const val VAD_HANGOVER_MS = 550L

        /** DEV smoke test: prompt the model once after setup to verify the full
         *  round-trip (video in → audio + transcript out) without a real mic.
         *  Set false once validated. */
        const val SMOKE_TEST = false
        val SYSTEM_PROMPT = """
            You are Point, a calm, expert repair assistant. The user points their phone
            camera at a physical object (often a power tool) and talks to you. Look at the
            frames, listen, and guide them one concrete step at a time in a natural spoken
            voice — short sentences, no lists read aloud.

            CONVERSATION RHYTHM — important:
            - After you finish a sentence or two, STOP and wait. Do not keep talking.
            - Never repeat yourself. If the user hasn't answered, stay silent — do not
              re-ask, re-explain, or narrate what you see. Wait for them.
            - Only speak again when the user speaks, or when the camera shows a clear,
              meaningful change (a part moved, removed, opened).

            FLOW — follow this order, do not skip ahead:
            1. Recognise the object → set toolName and recognizedLabel.
            2. Ask the user what's wrong and look at it. Talk it through.
            3. When you understand the fault, give a diagnosis: set diagnosisHeadline,
               diagnosisSupporting, diagnosisConfidence (0-100), diagnosisNextActions.
               This shows the user a diagnosis screen.
            4. ONLY AFTER the diagnosis, and only once the user asks you to guide them
               through the fix, set steps (short imperative phrases) and activeStepIndex 0.

            Do NOT invent steps before there is a diagnosis. If the user mentions a step
            number that doesn't exist yet, tell them you need to diagnose the problem
            first — do not fabricate a plan.
            Only send the fields that changed. Never read the JSON aloud.

            STEP PROGRESS:
            - Do not advance activeStepIndex in anticipation. Advance it when you can
              SEE in the camera that the step is done, or the user says it's done.
            - When you advance, say in one short phrase what you saw ("the cover's off —
              next, ...").
            - If you can't tell whether it's done, ask ONCE to see it, then wait. Do not
              ask again.
            - If the user asks to work on a different step or skip ahead, just set
              activeStepIndex to that step and follow them — don't argue.

            At the start of every response, call set_focus_region with a TIGHT bounding
            box around exactly the part you are about to talk about (a single screw, a
            latch, one cable) — not the whole tool unless you truly mean the whole tool.
            box is [ymin, xmin, ymax, xmax] as integers 0-1000 of the current frame.
            Update it the instant your focus moves. It drives an on-screen highlight.
            Keep replies brief and conversational — one or two sentences, like a person
            talking, not a written explanation.
        """.trimIndent()
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val audio = AndroidLiveAudioEngine(appContext.applicationContext)

    private val http = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val _connection = MutableStateFlow(AiConnectionState.IDLE)
    override val connection: StateFlow<AiConnectionState> = _connection.asStateFlow()

    private val _events = MutableSharedFlow<AiEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<AiEvent> = _events.asSharedFlow()

    private val _micLevel = MutableStateFlow(0f)
    override val micLevel: StateFlow<Float> = _micLevel.asStateFlow()

    private var ws: WebSocket? = null
    private var micJob: Job? = null

    /** True between start() and close(): survives transient websocket / mic drops so we can self-heal. */
    @Volatile private var wantLive = false

    @Volatile private var lastInputAt = 0L
    @Volatile private var awaitingReply = false
    /** True from the moment the user's turn ends until the model's turn completes. */
    @Volatile private var modelTurnActive = false
    /** Frames only stream while the user is engaged (talking / just talked). When
     *  idle we stop sending them so the model has nothing new to react to and stays
     *  quiet instead of narrating. */
    @Volatile private var engagedUntil = 0L

    // Model audio is played by a single consumer so chunks stay ordered and the
    // AudioTrack isn't reconfigured concurrently.
    private val playback = Channel<PcmChunk>(capacity = 64)
    private val playbackJob: Job = scope.launch {
        for (chunk in playback) runCatching { audio.play(chunk) }
    }

    override suspend fun start() {
        wantLive = true
        if (_connection.value == AiConnectionState.LIVE) {
            startMic() // re-arm if the mic dropped while the socket stayed up
            return
        }
        if (_connection.value == AiConnectionState.CONNECTING) return
        if (apiKey.isBlank()) {
            _connection.value = AiConnectionState.ERROR
            _events.tryEmit(AiEvent.Error("No GEMINI_API_KEY in local.properties"))
            return
        }
        _connection.value = AiConnectionState.CONNECTING
        println("[point-ai] (direct) connecting to $MODEL …")
        val request = Request.Builder().url("$WS_URL?key=$apiKey").build()
        ws = http.newWebSocket(request, Listener())
    }

    private inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            webSocket.send(setupMessage())
            println("[point-ai] (direct) socket open, setup sent")
        }

        override fun onMessage(webSocket: WebSocket, text: String) = handle(text)
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) = handle(bytes.utf8())

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            println("[point-ai] (direct) closing $code $reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            teardown(AiEvent.Closed)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("[point-ai] (direct) failure: $t ${response?.code}")
            _connection.value = AiConnectionState.ERROR
            teardown(AiEvent.Error(t.message ?: "websocket failure"))
        }
    }

    private fun handle(raw: String) {
        val root = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrNull() ?: return

        if (root["setupComplete"] != null) {
            _connection.value = AiConnectionState.LIVE
            println("[point-ai] (direct) setup complete — streaming mic")
            startMic()
            if (SMOKE_TEST) scope.launch {
                kotlinx.coroutines.delay(2500)
                println("[point-ai] (direct) SMOKE_TEST: sending prompt")
                sendText("Briefly greet me out loud and describe what you see in the camera.")
            }
            return
        }

        root["toolCall"]?.jsonObject?.get("functionCalls")?.jsonArray?.forEach { call ->
            val fc = call.jsonObject
            val name = fc["name"]?.jsonPrimitive?.content
            val args = fc["args"]?.jsonObject
            when {
                name == "set_repair_state" && args != null ->
                    _events.tryEmit(AiEvent.RepairStateUpdate(argsToPatch(args)))
                name == "set_focus_region" && args != null -> {
                    val box = args["box"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull }
                    if (box?.size == 4) _events.tryEmit(AiEvent.RepairStateUpdate(RepairStatePatch(focusBox = box)))
                }
            }
            val id = fc["id"]?.jsonPrimitive?.content
            ws?.send(buildJsonObject {
                putJsonObject("toolResponse") {
                    putJsonArray("functionResponses") {
                        addJsonObject {
                            if (id != null) put("id", id)
                            if (name != null) put("name", name)
                            putJsonObject("response") { put("result", "ok") }
                        }
                    }
                }
            }.toString())
        }

        val server = root["serverContent"]?.jsonObject ?: return

        server["inputTranscription"]?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?.takeIf { it.isNotBlank() }
            ?.let { _events.tryEmit(AiEvent.InputTranscript(it)) }

        server["outputTranscription"]?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?.takeIf { it.isNotBlank() }
            ?.let { _events.tryEmit(AiEvent.OutputTranscript(it)) }

        server["modelTurn"]?.jsonObject?.get("parts")?.jsonArray?.forEach { part ->
            val inline = part.jsonObject["inlineData"]?.jsonObject ?: return@forEach
            val mime = inline["mimeType"]?.jsonPrimitive?.content ?: return@forEach
            if (!mime.startsWith("audio/")) return@forEach
            if (awaitingReply) {
                awaitingReply = false
                modelTurnActive = true
                println("[point-ai] (direct) reply latency ${System.currentTimeMillis() - lastInputAt} ms (from activityEnd, wsQueue=${ws?.queueSize() ?: 0})")
            }
            val data = inline["data"]?.jsonPrimitive?.content ?: return@forEach
            val rate = Regex("rate=(\\d+)").find(mime)?.groupValues?.get(1)?.toIntOrNull()
                ?: LiveAudioEngine.PLAYBACK_SAMPLE_RATE
            val pcm = Base64.decode(data, Base64.DEFAULT)
            playback.trySend(PcmChunk(pcm, rate))
        }

        if (server["interrupted"]?.jsonPrimitive?.content == "true") {
            while (playback.tryReceive().isSuccess) { /* drop queued audio */ }
            audio.clearPlayback()
            _events.tryEmit(AiEvent.Interrupted)
        }
        if (server["turnComplete"]?.jsonPrimitive?.content == "true") {
            modelTurnActive = false
            _events.tryEmit(AiEvent.TurnComplete)
        }
    }

    private fun startMic() {
        if (micJob?.isActive == true) return
        micJob = scope.launch {
            var speaking = false
            var quietSince = 0L
            var loudCount = 0
            var logTick = 0
            runCatching {
                audio.startCapture().collect { chunk ->
                    // Full duplex: the platform AEC (comm mode) removes the speaker from
                    // the mic, so we always forward — the user can talk over the assistant.
                    val level = rms(chunk.bytes)
                    _micLevel.value = level
                    val now = System.currentTimeMillis()
                    if (level > VAD_START) engagedUntil = now + 5000

                    // Client-side VAD → explicit turn boundaries (server VAD is disabled).
                    if (!speaking) {
                        if (level > VAD_START) {
                            if (++loudCount >= 2) {
                                speaking = true; loudCount = 0
                                runCatching { ws?.send("""{"realtimeInput":{"activityStart":{}}}""") }
                            }
                        } else loudCount = 0
                    } else {
                        if (level < VAD_END) {
                            if (quietSince == 0L) quietSince = now
                            else if (now - quietSince > VAD_HANGOVER_MS) {
                                speaking = false; quietSince = 0L
                                lastInputAt = System.currentTimeMillis()
                                awaitingReply = true
                                runCatching { ws?.send("""{"realtimeInput":{"activityEnd":{}}}""") }
                            }
                        } else quietSince = 0L
                    }

                    if (++logTick % 100 == 0) println("[point-ai] (direct) rms=${(level * 100).toInt()} speaking=$speaking")

                    // Only stream the mic during (and just after) speech — no point
                    // sending room tone, and it keeps the uplink clear for the reply.
                    if (speaking || (quietSince != 0L && now - quietSince < VAD_HANGOVER_MS + 200)) {
                        val b64 = Base64.encodeToString(chunk.bytes, Base64.NO_WRAP)
                        ws?.send(realtimeBlob("audio", CAPTURE_MIME, b64))
                    }
                }
            }.onFailure { println("[point-ai] (direct) mic failed: $it") }
            _micLevel.value = 0f
            micJob = null
            // AudioRecord can be reclaimed on screen/route changes — bring it back.
            if (wantLive && _connection.value == AiConnectionState.LIVE) {
                kotlinx.coroutines.delay(300)
                startMic()
            }
        }
    }

    override fun stopConversation() {
        micJob?.cancel()
        micJob = null
        audio.stopCapture()
    }

    private var frameCount = 0
    override suspend fun sendVideoFrame(jpeg: ByteArray) {
        if (_connection.value != AiConnectionState.LIVE) return
        // Don't upload frames while the model is talking (it can't change its mind
        // mid-turn) or while the user is idle (nothing new to react to → stays quiet).
        if (modelTurnActive || System.currentTimeMillis() > engagedUntil) return
        val b64 = Base64.encodeToString(jpeg, Base64.NO_WRAP)
        ws?.send(realtimeBlob("video", "image/jpeg", b64))
        if (++frameCount % 10 == 1) println("[point-ai] (direct) sent frame #$frameCount (${jpeg.size} B)")
    }

    override suspend fun sendText(text: String) {
        val msg = buildJsonObject {
            putJsonObject("clientContent") {
                putJsonArray("turns") {
                    addJsonObject {
                        put("role", "user")
                        putJsonArray("parts") { addJsonObject { put("text", text) } }
                    }
                }
                put("turnComplete", true)
            }
        }
        ws?.send(msg.toString())
    }

    override suspend fun close() {
        wantLive = false
        stopConversation()
        while (playback.tryReceive().isSuccess) { /* drop queued audio */ }
        runCatching { ws?.close(1000, "client closing") }
        ws = null
        audio.release()
        // playbackJob + channel stay alive for the remembered session's lifetime,
        // so a later start() can resume without rebuilding them.
        _connection.value = AiConnectionState.IDLE
        _events.tryEmit(AiEvent.Closed)
    }

    private fun teardown(event: AiEvent) {
        stopConversation()
        audio.release()
        ws = null
        if (_connection.value != AiConnectionState.ERROR) _connection.value = AiConnectionState.IDLE
        _events.tryEmit(event)
        // Self-heal: if the caller still wants the session live, reconnect.
        if (wantLive) scope.launch {
            kotlinx.coroutines.delay(1500)
            if (wantLive && _connection.value != AiConnectionState.LIVE &&
                _connection.value != AiConnectionState.CONNECTING
            ) {
                println("[point-ai] (direct) reconnecting …")
                start()
            }
        }
    }

    /** Rough loudness of a 16-bit-LE PCM chunk, mapped to ~0..1 for the meter. */
    private fun rms(pcm: ByteArray): Float {
        if (pcm.size < 2) return 0f
        var sum = 0.0
        var i = 0
        val n = pcm.size / 2
        while (i < pcm.size - 1) {
            val s = (pcm[i].toInt() and 0xff) or (pcm[i + 1].toInt() shl 8)
            sum += (s.toShort().toInt()).let { it.toDouble() * it }
            i += 2
        }
        val rms = kotlin.math.sqrt(sum / n) / 32768.0
        // speech sits around 0.02..0.2 rms; expand that into a usable bar range
        return (rms * 6.0).coerceIn(0.0, 1.0).toFloat()
    }

    private fun argsToPatch(a: JsonObject): RepairStatePatch {
        fun str(k: String) = a[k]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
        fun int(k: String) = a[k]?.jsonPrimitive?.intOrNull
        fun list(k: String) = a[k]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
        return RepairStatePatch(
            toolName = str("toolName"),
            recognizedLabel = str("recognizedLabel"),
            steps = list("steps"),
            activeStepIndex = int("activeStepIndex"),
            instruction = str("instruction"),
            diagnosisHeadline = str("diagnosisHeadline"),
            diagnosisSupporting = str("diagnosisSupporting"),
            diagnosisConfidence = int("diagnosisConfidence"),
            diagnosisNextActions = list("diagnosisNextActions"),
        )
    }

    // --- message builders ---------------------------------------------------

    private fun setupMessage(): String = buildJsonObject {
        putJsonObject("setup") {
            put("model", MODEL)
            putJsonObject("generationConfig") {
                putJsonArray("responseModalities") { add("AUDIO") }
                // Skip the model's pre-speech "thinking" pass — it adds 1-3s of latency
                // before the voice starts, which reads as lag in a live conversation.
                putJsonObject("thinkingConfig") { put("thinkingBudget", 0) }
            }
            putJsonObject("systemInstruction") {
                putJsonArray("parts") { addJsonObject { put("text", SYSTEM_PROMPT) } }
            }
            putJsonObject("inputAudioTranscription") {}
            putJsonObject("outputAudioTranscription") {}
            // Keep the live context bounded — audio + frames pile up fast and an
            // unbounded window makes every turn slower than the last.
            putJsonObject("contextWindowCompression") {
                put("triggerTokens", "12000")
                putJsonObject("slidingWindow") { put("targetTokens", "6000") }
            }
            // Server-side VAD: low start sensitivity so any echo the hardware AEC
            // misses doesn't false-trigger a turn; short silence for snappy hand-off.
            putJsonObject("realtimeInputConfig") {
                // We run our own client-side VAD off the mic RMS and signal turn
                // boundaries explicitly — deterministic and instant, vs. the server
                // guessing from a continuous stream (which was adding ~4 s).
                putJsonObject("automaticActivityDetection") { put("disabled", true) }
            }
            putJsonArray("tools") {
                addJsonObject {
                    putJsonArray("functionDeclarations") {
                        addJsonObject { setRepairStateDecl() }
                        addJsonObject { setFocusRegionDecl() }
                    }
                }
            }
        }
    }.toString()

    private fun kotlinx.serialization.json.JsonObjectBuilder.setRepairStateDecl() {
        put("name", "set_repair_state")
        put("description", "Update the on-screen repair state. Send only changed fields.")
        putJsonObject("parameters") {
            put("type", "OBJECT")
            putJsonObject("properties") {
                fun str(k: String) = putJsonObject(k) { put("type", "STRING") }
                fun int(k: String) = putJsonObject(k) { put("type", "INTEGER") }
                fun strArr(k: String) = putJsonObject(k) {
                    put("type", "ARRAY"); putJsonObject("items") { put("type", "STRING") }
                }
                str("toolName"); str("recognizedLabel")
                strArr("steps"); int("activeStepIndex"); str("instruction")
                str("diagnosisHeadline"); str("diagnosisSupporting")
                int("diagnosisConfidence"); strArr("diagnosisNextActions")
            }
        }
    }

    private fun kotlinx.serialization.json.JsonObjectBuilder.setFocusRegionDecl() {
        put("name", "set_focus_region")
        put(
            "description",
            "Highlight on screen the exact part you are talking about. box is " +
                "[ymin, xmin, ymax, xmax] as integers 0-1000 of the current camera frame. " +
                "Call it every time your point of focus moves.",
        )
        putJsonObject("parameters") {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("box") {
                    put("type", "ARRAY")
                    putJsonObject("items") { put("type", "INTEGER") }
                }
            }
            putJsonArray("required") { add("box") }
        }
    }

    /** Current Live-API realtime input: typed `audio` / `video` blobs (not the
     *  legacy `mediaChunks` array, which the native-audio models ignore). */
    private fun realtimeBlob(field: String, mime: String, base64: String): String = buildJsonObject {
        putJsonObject("realtimeInput") {
            putJsonObject(field) {
                put("mimeType", mime)
                put("data", base64)
            }
        }
    }.toString()
}

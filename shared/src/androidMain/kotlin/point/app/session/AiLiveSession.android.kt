@file:OptIn(com.google.firebase.ai.type.PublicPreviewAPI::class)

package point.app.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.InlineData
import com.google.firebase.ai.type.LiveServerContent
import com.google.firebase.ai.type.LiveServerToolCall
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.Transcription
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private const val LIVE_MODEL = "gemini-2.5-flash-native-audio-preview-12-2025"

private val SYSTEM_PROMPT = """
You are Point, a calm, expert repair assistant. The user points their phone
camera at a physical object (often a power tool) and talks to you. Look at the
frames, listen, and guide them one concrete step at a time in a natural spoken
voice — short sentences, no lists read aloud.

Whenever the repair plan, the current step, the on-screen instruction, or the
diagnosis changes, call the set_repair_state tool so the screen stays in sync.
Call it as soon as you have recognised the tool, and again each time the user
completes a step.
""".trimIndent()

// TODO(Phase 8b): re-add the `set_repair_state` function tool once the Schema
// factory API (Schema.str / Schema.numInt) resolves against firebase-ai. For now
// the model drives voice + transcripts; the screen state is updated by parsing
// the model's spoken JSON, or wired via the tool in a follow-up.

private class FirebaseAiLiveSession : AiLiveSession {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _connection = MutableStateFlow(AiConnectionState.IDLE)
    override val connection: StateFlow<AiConnectionState> = _connection.asStateFlow()

    private val _events = MutableSharedFlow<AiEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<AiEvent> = _events.asSharedFlow()

    // The Firebase SDK owns the mic, so no live level is available here.
    override val micLevel: StateFlow<Float> = MutableStateFlow(0f).asStateFlow()

    private var session: LiveSession? = null
    private var receiveJob: Job? = null
    private var conversationJob: Job? = null

    override suspend fun start() {
        if (_connection.value == AiConnectionState.LIVE || _connection.value == AiConnectionState.CONNECTING) return
        _connection.value = AiConnectionState.CONNECTING
        println("[point-ai] connecting to $LIVE_MODEL …")
        try {
            val model = Firebase.ai(backend = GenerativeBackend.googleAI()).liveModel(
                modelName = LIVE_MODEL,
                generationConfig = liveGenerationConfig {
                    responseModality = ResponseModality.AUDIO
                },
                systemInstruction = Content(parts = listOf(TextPart(SYSTEM_PROMPT))),
            )
            val live = model.connect()
            session = live
            _connection.value = AiConnectionState.LIVE
            println("[point-ai] connected — starting audio conversation")

            receiveJob = scope.launch {
                runCatching {
                    live.receive().collect { message ->
                        when (message) {
                            is LiveServerContent -> {
                                if (message.interrupted) _events.tryEmit(AiEvent.Interrupted)
                                if (message.turnComplete) _events.tryEmit(AiEvent.TurnComplete)
                            }
                            is LiveServerToolCall -> message.functionCalls.forEach {
                                _events.tryEmit(AiEvent.RepairStateUpdate(it.toPatch()))
                            }
                        }
                    }
                }.onFailure { println("[point-ai] receive() failed: $it") }
            }

            conversationJob = scope.launch {
                runCatching {
                    live.startAudioConversation(
                        functionCallHandler = { fc -> handleFunctionCall(fc) },
                        transcriptHandler = { input: Transcription?, output: Transcription? ->
                            input?.text?.takeIf { it.isNotBlank() }?.let {
                                println("[point-ai] user: $it")
                                _events.tryEmit(AiEvent.InputTranscript(it))
                            }
                            output?.text?.takeIf { it.isNotBlank() }?.let {
                                println("[point-ai] model: $it")
                                _events.tryEmit(AiEvent.OutputTranscript(it))
                            }
                        },
                        enableInterruptions = true,
                    )
                }.onFailure { println("[point-ai] startAudioConversation failed: $it") }
            }
        } catch (t: Throwable) {
            println("[point-ai] connect failed: $t")
            _connection.value = AiConnectionState.ERROR
            _events.tryEmit(AiEvent.Error(t.message ?: "connect failed"))
        }
    }

    private fun handleFunctionCall(fc: FunctionCallPart): FunctionResponsePart {
        _events.tryEmit(AiEvent.RepairStateUpdate(fc.toPatch()))
        return FunctionResponsePart(
            fc.name,
            JsonObject(mapOf("status" to JsonPrimitive("ok"))),
        )
    }

    override fun stopConversation() {
        runCatching { session?.stopAudioConversation() }
    }

    private var frameCount = 0
    override suspend fun sendVideoFrame(jpeg: ByteArray) {
        runCatching {
            session?.sendVideoRealtime(InlineData(jpeg, "image/jpeg"))
            if (++frameCount % 5 == 1) println("[point-ai] sent frame #$frameCount (${jpeg.size} B)")
        }.onFailure { println("[point-ai] sendVideoFrame failed: $it") }
    }

    override suspend fun sendText(text: String) {
        runCatching { session?.sendTextRealtime(text) }
    }

    override suspend fun close() {
        conversationJob?.cancel()
        receiveJob?.cancel()
        runCatching { session?.stopAudioConversation() }
        runCatching { session?.stopReceiving() }
        session = null
        _connection.value = AiConnectionState.IDLE
        _events.tryEmit(AiEvent.Closed)
    }
}

private fun FunctionCallPart.toPatch(): RepairStatePatch {
    val a: Map<String, JsonElement> = args
    fun prim(k: String) = a[k] as? JsonPrimitive
    fun str(k: String) = prim(k)?.contentOrNull
    fun int(k: String) = prim(k)?.intOrNull
    fun list(k: String) = a[k]?.let {
        runCatching { it.jsonArray.map { e -> e.jsonPrimitive.content } }.getOrNull()
    }
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

@Composable
actual fun rememberAiLiveSession(): AiLiveSession {
    // DEV-ONLY: direct Gemini Live websocket transport while the Firebase project's
    // Gemini access is being provisioned. Swap back to `FirebaseAiLiveSession()` for
    // production — the `AiLiveSession` interface is unchanged.
    val context = LocalContext.current
    return remember { DirectGeminiLiveSession(context.applicationContext, GeminiDevKey.value) }
}

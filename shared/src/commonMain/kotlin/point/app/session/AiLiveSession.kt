package point.app.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.SharedFlow

/** A partial update to the repair session, produced by the model's `set_repair_state` tool call. */
class RepairStatePatch(
    val toolName: String? = null,
    val recognizedLabel: String? = null,
    val steps: List<String>? = null,
    val activeStepIndex: Int? = null,
    val instruction: String? = null,
    val diagnosisHeadline: String? = null,
    val diagnosisSupporting: String? = null,
    val diagnosisConfidence: Int? = null,
    val diagnosisNextActions: List<String>? = null,
    /** [ymin, xmin, ymax, xmax] on a 0..1000 scale — the part the model is pointing at. */
    val focusBox: List<Int>? = null,
)

sealed interface AiEvent {
    /** Transcript of what the user said. */
    data class InputTranscript(val text: String) : AiEvent
    /** Transcript of what the model said. */
    data class OutputTranscript(val text: String) : AiEvent
    /** The model updated the on-screen repair state via `set_repair_state`. */
    data class RepairStateUpdate(val patch: RepairStatePatch) : AiEvent
    data object TurnComplete : AiEvent
    data object Interrupted : AiEvent
    data object Closed : AiEvent
    data class Error(val message: String) : AiEvent
}

enum class AiConnectionState { IDLE, CONNECTING, LIVE, ERROR }

/**
 * The Gemini Live loop, one abstraction over the platform SDK. Android uses the
 * Firebase AI Logic Live API (`gemini-*-live`); iOS is Phase 9.
 */
interface AiLiveSession {
    val connection: kotlinx.coroutines.flow.StateFlow<AiConnectionState>
    val events: SharedFlow<AiEvent>
    /** Live microphone input level, 0f (silent) .. 1f (loud). Drives the listening UI. */
    val micLevel: kotlinx.coroutines.flow.StateFlow<Float>

    /** Open the websocket + start the SDK-managed voice conversation (mic + speaker). */
    suspend fun start()
    /** Stop the voice conversation but keep the session open. */
    fun stopConversation()
    /** One downscaled JPEG camera frame (~1 fps). */
    suspend fun sendVideoFrame(jpeg: ByteArray)
    /** Typed-question fallback. */
    suspend fun sendText(text: String)
    suspend fun close()
}

@Composable
expect fun rememberAiLiveSession(): AiLiveSession

val LocalAiLiveSession = staticCompositionLocalOf<AiLiveSession> {
    error("AiLiveSession not provided — wrap content in PointApp()")
}

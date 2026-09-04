package point.app.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/** iOS Gemini Live (Firebase AI Logic Apple SDK) is Phase 9 — stub for now. */
private class IosAiLiveSessionStub : AiLiveSession {
    override val connection: StateFlow<AiConnectionState> = MutableStateFlow(AiConnectionState.IDLE).asStateFlow()
    override val events: SharedFlow<AiEvent> = MutableSharedFlow<AiEvent>().asSharedFlow()
    override val micLevel: StateFlow<Float> = MutableStateFlow(0f).asStateFlow()
    override suspend fun start() {}
    override fun stopConversation() {}
    override suspend fun sendVideoFrame(jpeg: ByteArray) {}
    override suspend fun sendText(text: String) {}
    override suspend fun close() {}
}

@Composable
actual fun rememberAiLiveSession(): AiLiveSession = remember { IosAiLiveSessionStub() }

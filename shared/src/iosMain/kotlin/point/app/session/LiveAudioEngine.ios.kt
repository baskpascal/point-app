package point.app.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

/** iOS audio (AVAudioEngine / AVAudioPlayerNode) is Phase 9 — stub for now. */
private class IosAudioStub : LiveAudioEngine {
    override val permission: StateFlow<AudioPermission> = MutableStateFlow(AudioPermission.UNKNOWN).asStateFlow()
    override val isCapturing: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    override fun requestPermission() {}
    override fun startCapture(): Flow<PcmChunk> = emptyFlow()
    override fun stopCapture() {}
    override suspend fun play(chunk: PcmChunk) {}
    override fun clearPlayback() {}
    override fun release() {}
}

@Composable
actual fun rememberLiveAudioEngine(): LiveAudioEngine = remember { IosAudioStub() }

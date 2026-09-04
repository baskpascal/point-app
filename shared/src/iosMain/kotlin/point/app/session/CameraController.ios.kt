package point.app.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** iOS camera (AVFoundation) is Phase 9 — this stub keeps commonMain building. */
private class IosCameraStub : CameraController {
    override val permission: StateFlow<CameraPermission> = MutableStateFlow(CameraPermission.UNKNOWN).asStateFlow()
    override val isRunning: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    override val frames: SharedFlow<CameraFrame> = MutableSharedFlow()
    override fun requestPermission() {}
    override fun start() {}
    override fun stop() {}
}

@Composable
actual fun rememberCameraController(): CameraController = remember { IosCameraStub() }

@Composable
actual fun CameraPreview(controller: CameraController, modifier: Modifier) {
    // No-op: the screen shows its dark placeholder background until Phase 9.
}

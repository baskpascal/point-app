package point.app.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

enum class CameraPermission { UNKNOWN, GRANTED, DENIED }

/** A downscaled JPEG frame for the AI session (wired in Phase 8). */
class CameraFrame(
    val jpeg: ByteArray,
    val width: Int,
    val height: Int,
    val timestampMs: Long,
)

/**
 * Cross-platform camera. Phase 6 implements the Android preview (CameraX); the
 * `frames` stream and iOS (AVFoundation) land in Phases 8 and 9.
 */
interface CameraController {
    val permission: StateFlow<CameraPermission>
    val isRunning: StateFlow<Boolean>
    val frames: SharedFlow<CameraFrame>

    /** Ask the OS for camera permission (no-op if already granted/denied-permanently). */
    fun requestPermission()
    fun start()
    fun stop()
}

/** Returns the platform camera, bound to the current composition's lifecycle. */
@Composable
expect fun rememberCameraController(): CameraController

/** One shared camera per app — provided at the root, read by screens + the AI bridge. */
val LocalCameraController = staticCompositionLocalOf<CameraController> {
    error("CameraController not provided — wrap content in PointApp()")
}

/** Full-bleed live preview. Renders nothing when the platform has no camera impl yet. */
@Composable
expect fun CameraPreview(controller: CameraController, modifier: Modifier)

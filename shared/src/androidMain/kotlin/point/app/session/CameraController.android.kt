package point.app.session

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import android.util.Size
import java.io.ByteArrayOutputStream
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private class AndroidCameraController(
    private val appContext: Context,
) : CameraController {

    private val _permission = MutableStateFlow(CameraPermission.UNKNOWN)
    override val permission: StateFlow<CameraPermission> = _permission.asStateFlow()

    private val _running = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _running.asStateFlow()

    private val _frames = MutableSharedFlow<CameraFrame>(extraBufferCapacity = 2)
    override val frames: SharedFlow<CameraFrame> = _frames

    @Volatile private var lastFrameAt = 0L
    private val analysisExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()

    private var lifecycleOwner: LifecycleOwner? = null
    private var previewView: PreviewView? = null
    private var requestLauncher: (() -> Unit)? = null
    private var provider: ProcessCameraProvider? = null

    fun refreshPermission() {
        val granted = ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            _permission.value = CameraPermission.GRANTED
            bindIfReady()
        } else if (_permission.value != CameraPermission.DENIED) {
            _permission.value = CameraPermission.UNKNOWN
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _permission.value = if (granted) CameraPermission.GRANTED else CameraPermission.DENIED
        if (granted) bindIfReady()
    }

    fun attach(owner: LifecycleOwner, launcher: () -> Unit) {
        lifecycleOwner = owner
        requestLauncher = launcher
        refreshPermission()
    }

    fun detach() {
        provider?.unbindAll()
        _running.value = false
        lifecycleOwner = null
        previewView = null
        requestLauncher = null
    }

    fun bindPreview(view: PreviewView) {
        previewView = view
        bindIfReady()
    }

    private fun bindIfReady() {
        val owner = lifecycleOwner ?: return
        val view = previewView ?: return
        if (_permission.value != CameraPermission.GRANTED) return

        val future = ProcessCameraProvider.getInstance(appContext)
        future.addListener({
            val cameraProvider = future.get()
            provider = cameraProvider
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(view.surfaceProvider)

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(Size(1024, 768), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER),
                        )
                        .build(),
                )
                .build()
            analysis.setAnalyzer(analysisExecutor) { proxy -> onFrame(proxy) }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis,
                )
                _running.value = true
            } catch (_: Exception) {
                _running.value = false
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    private fun onFrame(proxy: ImageProxy) {
        try {
            val now = System.currentTimeMillis()
            if (now - lastFrameAt < 1500L) return // ~0.7 fps is plenty for the model
            lastFrameAt = now
            val bmp: Bitmap = proxy.toBitmap()
            val m = Matrix().apply {
                if (proxy.imageInfo.rotationDegrees != 0) postRotate(proxy.imageInfo.rotationDegrees.toFloat())
                // Downscale to ~640 px on the long edge — keeps the upload small so it
                // doesn't compete with the audio stream and add reply latency.
                val longEdge = maxOf(bmp.width, bmp.height)
                if (longEdge > 640) { val s = 640f / longEdge; postScale(s, s) }
            }
            val processed = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
            val out = ByteArrayOutputStream()
            processed.compress(Bitmap.CompressFormat.JPEG, 55, out)
            _frames.tryEmit(CameraFrame(out.toByteArray(), processed.width, processed.height, now))
        } catch (_: Throwable) {
            // drop the frame
        } finally {
            proxy.close()
        }
    }

    override fun requestPermission() {
        if (_permission.value == CameraPermission.GRANTED) return
        // Re-invoking the system launcher is safe even after a prior denial: Android
        // shows the dialog again unless the user picked "Don't ask again" — in that
        // case it just re-delivers `false` instantly, which is why we also expose
        // openAppSettings() as a fallback the UI can offer.
        requestLauncher?.invoke()
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", appContext.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(intent)
    }

    override fun start() = bindIfReady()

    override fun stop() {
        provider?.unbindAll()
        _running.value = false
    }
}

@Composable
actual fun rememberCameraController(): CameraController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { AndroidCameraController(context.applicationContext) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> controller.onPermissionResult(granted) }

    DisposableEffect(lifecycleOwner) {
        controller.attach(lifecycleOwner) { launcher.launch(Manifest.permission.CAMERA) }
        // If the user backed out to system Settings to grant the permission manually
        // (the "open app settings" fallback below), the Activity's LifecycleOwner
        // never changes — so re-check on every resume, not just once on attach.
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) controller.refreshPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.detach()
        }
    }
    return controller
}

@Composable
actual fun CameraPreview(controller: CameraController, modifier: Modifier) {
    val android = controller as? AndroidCameraController ?: return
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }.also { android.bindPreview(it) }
        },
    )
}

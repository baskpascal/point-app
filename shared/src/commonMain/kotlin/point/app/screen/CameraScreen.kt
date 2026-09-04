package point.app.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import dev.chrisbanes.haze.HazeState
import point.app.design.PointTheme
import point.app.design.ds
import point.app.ui.foundation.BlinkingDot
import point.app.ui.foundation.CameraBackground
import point.app.ui.foundation.CircleGlassButton
import point.app.ui.foundation.GradientButton
import point.app.ui.foundation.LocalHazeState
import point.app.ui.foundation.PText
import point.app.ui.foundation.cameraVignette
import point.app.ui.foundation.glass
import point.app.ui.foundation.glassSource
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import point.app.session.CameraPermission
import point.app.session.CameraPreview
import point.app.session.rememberCameraController
import point.app.state.LocalRepairViewModel

@Composable
fun CameraScreen(onClose: () -> Unit, onAsk: () -> Unit) {
    val c = PointTheme.colors
    val t = PointTheme.type
    val haze = remember { HazeState() }
    val vm = LocalRepairViewModel.current
    val state by vm.state.collectAsState()
    val cam = point.app.session.LocalCameraController.current
    val perm by cam.permission.collectAsState()
    LaunchedEffect(perm) { if (perm == CameraPermission.UNKNOWN) cam.requestPermission() }

    CompositionLocalProvider(LocalHazeState provides haze) {
        Box(Modifier.fillMaxSize()) {
            // Live camera + placeholder fallback, marked as the blur source.
            Box(Modifier.fillMaxSize().glassSource(haze)) {
                Box(Modifier.matchParentSize().then(CameraBackground()))
                if (perm == CameraPermission.GRANTED) {
                    CameraPreview(cam, Modifier.matchParentSize())
                }
                Box(Modifier.matchParentSize().cameraVignette())
            }

            if (perm == CameraPermission.DENIED) {
                CameraPermissionDeniedPanel(
                    onRetry = { cam.requestPermission() },
                    onOpenSettings = { cam.openAppSettings() },
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = ds(28)),
                )
            }

            point.app.ui.foundation.FocusHighlight(state.focusBox, state.frameAspect, Modifier.matchParentSize())

            CircleGlassButton(
                glyph = "✕",
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopStart).padding(start = ds(19), top = ds(24)),
                diameter = ds(44),
            )

            // Recognized pill — only once the model has identified the tool
            if (state.hasTool) {
                Row(
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = ds(88))
                        .height(ds(42))
                        .glass(RoundedCornerShape(ds(21)), tint = c.glass80)
                        .padding(horizontal = ds(20)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ds(11)),
                ) {
                    Box(Modifier.size(ds(12)).clip(CircleShape).background(PointTheme.gradients.accentDiagonal))
                    PText(state.tool.recognizedLabel, t.caption, c.ink)
                }

                RecognitionFrame(
                    Modifier.align(Alignment.TopStart).padding(start = ds(112), top = ds(236)).size(ds(158)),
                )
            }

            // Live Camera badge
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = ds(22), bottom = ds(168))
                    .height(ds(34))
                    .glass(RoundedCornerShape(ds(17)), tint = c.glass80)
                    .padding(horizontal = ds(15)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ds(9)),
            ) {
                BlinkingDot(c.greenDot, ds(9))
                PText("Live Camera", t.micro, c.ink)
            }

            // Voice dock
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = ds(20), end = ds(20), bottom = ds(30))
                    .height(ds(76))
                    .glass(RoundedCornerShape(ds(38)), tint = c.glass78, border = c.stroke80)
                    .padding(start = ds(26), end = ds(8))
                    .clickable(onClick = onAsk),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PText("What do you want to fix?", t.bodyMedium, c.ink)
                MicButton(onClick = onAsk)
            }
        }
    }
}

@Composable
private fun CameraPermissionDeniedPanel(
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PointTheme.colors
    val t = PointTheme.type
    Column(
        modifier
            .glass(RoundedCornerShape(ds(24)), tint = c.glass80, border = c.stroke80)
            .padding(ds(24)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ds(16)),
    ) {
        PText(
            "Point precisa da câmera para reconhecer a ferramenta",
            t.bodyMedium,
            c.ink,
            align = TextAlign.Center,
        )
        PText(
            "Você negou o acesso à câmera. Toque em tentar de novo, ou abra as configurações do app para ativar a permissão manualmente.",
            t.caption,
            c.ink.copy(alpha = 0.72f),
            align = TextAlign.Center,
        )
        GradientButton(label = "Tentar novamente", onClick = onRetry, height = ds(52))
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onOpenSettings),
            horizontalArrangement = Arrangement.Center,
        ) {
            PText("Abrir configurações do app", t.caption, c.ink, align = TextAlign.Center)
        }
    }
}

@Composable
fun RecognitionFrame(modifier: Modifier) {
    val corner = ds(34)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val strokePx = with(density) { ds(2.5f).toPx() }
    val radiusPx = with(density) { ds(12).toPx() }
    val color = PointTheme.colors.white.copy(alpha = 0.95f)
    Box(modifier) {
        listOf(
            Alignment.TopStart to (true to true),
            Alignment.TopEnd to (true to false),
            Alignment.BottomStart to (false to true),
            Alignment.BottomEnd to (false to false),
        ).forEach { (align, corners) ->
            val (top, start) = corners
            Box(
                Modifier.align(align).size(corner).drawBehind {
                    val len = size.width
                    val r = radiusPx
                    val path = Path()
                    // vertical arm end
                    val vEndY = if (top) len else 0f
                    val cornerY = if (top) r else len - r
                    val cornerX = if (start) r else len - r
                    val hEndX = if (start) len else 0f
                    path.moveTo(if (start) 0f else len, vEndY)
                    path.lineTo(if (start) 0f else len, cornerY)
                    path.quadraticTo(
                        if (start) 0f else len, if (top) 0f else len,
                        cornerX, if (top) 0f else len,
                    )
                    path.lineTo(hEndX, if (top) 0f else len)
                    drawPath(path, color, style = Stroke(width = strokePx, cap = StrokeCap.Round))
                },
            )
        }
    }
}

@Composable
private fun MicButton(onClick: () -> Unit) {
    val c = PointTheme.colors
    Box(
        Modifier
            .size(ds(60))
            .clip(CircleShape)
            .background(PointTheme.gradients.accentDiagonal)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .width(ds(11))
                .height(ds(19))
                .clip(RoundedCornerShape(ds(6)))
                .background(c.white),
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = ds(15))
                .width(ds(19))
                .height(ds(9))
                .border(ds(2), c.white, RoundedCornerShape(bottomStart = ds(10), bottomEnd = ds(10))),
        )
    }
}

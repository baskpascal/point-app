package point.app.ui.foundation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Screen background washes — 1:1 with the Claude Design canvas
 * (`Point Repair App.dc.html`). Each light screen is a vertical base gradient
 * plus one or two soft off-center radial glows; the camera screens are a near
 * solid tone under a top/bottom vignette (the photo plate sits between).
 */

private data class Glow(val color: Color, val fx: Float, val fy: Float, val rFrac: Float)

@Composable
private fun lightWash(base: Pair<Color, Color>, glows: List<Glow>): Modifier =
    Modifier.fillMaxSize().drawBehind {
        drawRect(Brush.verticalGradient(listOf(base.first, base.second)))
        val maxDim = size.maxDimension
        glows.forEach { g ->
            drawRect(
                Brush.radialGradient(
                    colors = listOf(g.color, g.color.copy(alpha = 0f)),
                    center = Offset(size.width * g.fx, size.height * g.fy),
                    radius = maxDim * g.rFrac,
                ),
            )
        }
    }

@Composable
fun SplashBackground(): Modifier = lightWash(
    base = Color(0xFFF7F5F3) to Color(0xFFF1EFEE),
    glows = listOf(
        Glow(Color(0xFFFFE3D2).copy(alpha = 0.85f), fx = 0.06f, fy = 0.16f, rFrac = 0.78f),
        Glow(Color(0xFFDBE6F5).copy(alpha = 0.80f), fx = 0.96f, fy = 0.98f, rFrac = 0.72f),
    ),
)

@Composable
fun TimelineBackground(): Modifier = lightWash(
    base = Color(0xFFF6F4F3) to Color(0xFFECEAEA),
    glows = listOf(Glow(Color(0xFFFFEEE4), fx = 0.80f, fy = 0.04f, rFrac = 0.75f)),
)

@Composable
fun DiagnosisBackground(): Modifier = lightWash(
    base = Color(0xFFF6F3F2) to Color(0xFFEFEDEE),
    glows = listOf(
        Glow(Color(0xFFFFE6D5), fx = 0.85f, fy = 0.02f, rFrac = 0.70f),
        Glow(Color(0xFFF2ECEC), fx = 0.05f, fy = 0.30f, rFrac = 0.60f),
    ),
)

@Composable
fun PremiumBackground(): Modifier = lightWash(
    base = Color(0xFFF6F4F3) to Color(0xFFF0EEEF),
    glows = listOf(
        Glow(Color(0xFFFFE3D3), fx = 0.60f, fy = 0.00f, rFrac = 0.75f),
        Glow(Color(0xFFE2E9F6), fx = 0.00f, fy = 1.00f, rFrac = 0.70f),
    ),
)

/**
 * Placeholder camera texture — a dark off-center radial so the glass overlays
 * have something to blur before the real CameraX/AVFoundation feed lands
 * (Phase 6). Not flat black.
 */
@Composable
fun CameraBackground(): Modifier = Modifier.fillMaxSize().drawBehind {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF221C19), Color(0xFF120E0C))))
    drawRect(
        Brush.radialGradient(
            colors = listOf(Color(0xFF3A302B).copy(alpha = 0.55f), Color(0x00000000)),
            center = Offset(size.width * 0.62f, size.height * 0.42f),
            radius = size.maxDimension * 0.7f,
        ),
    )
}

@Composable
fun GuidanceCameraBackground(): Modifier = Modifier.fillMaxSize().drawBehind {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF1E1613), Color(0xFF0E0B09))))
    drawRect(
        Brush.radialGradient(
            colors = listOf(Color(0xFF352824).copy(alpha = 0.5f), Color(0x00000000)),
            center = Offset(size.width * 0.34f, size.height * 0.52f),
            radius = size.maxDimension * 0.65f,
        ),
    )
}

/** Top+bottom vignette painted over the camera photo plate. */
fun Modifier.cameraVignette(): Modifier = this.drawBehind {
    drawRect(
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF14100E).copy(alpha = 0.28f),
                0.22f to Color.Transparent,
                0.62f to Color.Transparent,
                1.00f to Color(0xFF14100E).copy(alpha = 0.34f),
            ),
        ),
    )
}

fun Modifier.guidanceVignette(): Modifier = this.drawBehind {
    drawRect(
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF120E0C).copy(alpha = 0.30f),
                0.18f to Color.Transparent,
                0.55f to Color(0xFF120E0C).copy(alpha = 0.08f),
                1.00f to Color(0xFF120E0C).copy(alpha = 0.42f),
            ),
        ),
    )
}

package point.app.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Accent gradients from the canvas (`Point Repair App.dc.html`). The canvas uses
 * two coral→orange ramps:
 *  - `linear-gradient(140deg,#f8544e,#ffa23a)` — logo dot, mic button, small chips
 *  - `linear-gradient(100deg,#f4425f,#ff9a3d)` — the large CTA buttons
 */
@Immutable
class PointGradients(c: PointColors) {

    /** ~140deg: top-left → bottom-right. */
    val accentDiagonal: Brush = Brush.linearGradient(
        colors = listOf(c.coralDeep, c.orangeWarm),
        start = Offset.Zero,
        end = Offset.Infinite,
    )

    /** ~100deg: left → right, slight downward. */
    val ctaWide: Brush = Brush.linearGradient(
        colors = listOf(c.coral, c.orange),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, 40f),
    )

    /** Conic ramp for the confidence ring (`conic-gradient(from 205deg …)`). */
    val ringSweep: List<Color> = listOf(
        Color(0xFFF4525F),
        Color(0xFFF96B4A),
        Color(0xFFFF9A3D),
    )
}

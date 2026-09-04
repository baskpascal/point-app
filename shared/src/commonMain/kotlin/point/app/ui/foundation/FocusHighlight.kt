package point.app.ui.foundation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import point.app.design.PointTheme
import point.app.state.FocusBox

/**
 * The live "focus" highlight — a rounded outline over the part of the camera
 * frame the assistant is pointing at. Snaps to each new box with a stiff spring
 * (fast, tiny settle) and breathes gently so it reads as actively tracking
 * between the model's updates.
 *
 * [frameAspect] (frame w/h) corrects for the centre-cropped (`FILL_CENTER`)
 * preview so the box lands on the right pixels, not stretched.
 */
@Composable
fun FocusHighlight(box: FocusBox?, frameAspect: Float, modifier: Modifier = Modifier) {
    var last by remember { mutableStateOf<FocusBox?>(null) }
    if (box != null) last = box
    val target = box ?: last ?: return

    val snap = spring<Float>(stiffness = 1100f, dampingRatio = 0.82f)
    val left by animateFloatAsState(target.left, snap, label = "l")
    val top by animateFloatAsState(target.top, snap, label = "t")
    val right by animateFloatAsState(target.right, snap, label = "r")
    val bottom by animateFloatAsState(target.bottom, snap, label = "b")

    val presence by animateFloatAsState(
        targetValue = if (box != null) 1f else 0f,
        animationSpec = tween(if (box != null) 150 else 220),
        label = "presence",
    )
    if (presence <= 0.01f) return

    val breathe = rememberInfiniteTransition(label = "focusBreathe")
    val glow by breathe.animateFloat(
        0.55f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "glow",
    )
    val c = PointTheme.colors

    Canvas(modifier) {
        val canvasAspect = size.width / size.height
        val k = (if (frameAspect > 0f) frameAspect else 0.75f) / canvasAspect

        // Map a normalised frame coord to a normalised canvas coord under FILL_CENTER.
        fun mapX(nx: Float) = if (k > 1f) nx * k - (k - 1f) / 2f else nx
        fun mapY(ny: Float) = if (k > 1f) ny else ny / k - (1f / k - 1f) / 2f

        val x0 = mapX(left) * size.width
        val x1 = mapX(right) * size.width
        val y0 = mapY(top) * size.height
        val y1 = mapY(bottom) * size.height
        val scale = 0.94f + 0.06f * presence
        val cx = (x0 + x1) / 2f
        val cy = (y0 + y1) / 2f
        val rw = ((x1 - x0).coerceAtLeast(28f)) * scale
        val rh = ((y1 - y0).coerceAtLeast(28f)) * scale
        val tl = Offset(cx - rw / 2f, cy - rh / 2f)
        val sz = Size(rw, rh)
        val rad = CornerRadius(minOf(rw, rh) * 0.36f, minOf(rw, rh) * 0.36f)

        drawRoundRect(c.coralHot.copy(alpha = 0.20f * presence * glow), tl, sz, rad, Stroke(11.dp.toPx()))
        drawRoundRect(c.white.copy(alpha = 0.95f * presence), tl, sz, rad, Stroke(2.5.dp.toPx()))
    }
}

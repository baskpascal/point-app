package point.app.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Soft shadows — canvas values (`Point Repair App.dc.html`). Compose's `shadow()`
 * only approximates the canvas's long, wide, offset drop shadows; Phase 11
 * swaps in a real blurred-layer implementation.
 */

private val ambient = Color(0xFF32323C).copy(alpha = 0.28f)   // rgba(40,40,60,.45) softened
private val glow = Color(0xFFF4425F).copy(alpha = 0.45f)

fun Modifier.softShadow(shape: Shape = RoundedCornerShape(28.dp), elevation: androidx.compose.ui.unit.Dp = 16.dp): Modifier =
    this.shadow(elevation = elevation, shape = shape, clip = false, ambientColor = ambient, spotColor = ambient)

fun Modifier.glowShadow(shape: Shape = RoundedCornerShape(32.dp), elevation: androidx.compose.ui.unit.Dp = 14.dp): Modifier =
    this.shadow(elevation = elevation, shape = shape, clip = false, ambientColor = glow, spotColor = glow)

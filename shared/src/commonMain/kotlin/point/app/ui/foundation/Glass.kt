package point.app.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import point.app.design.PointTheme

/**
 * Shared Haze state for one screen. The screen's background / camera layer marks
 * itself [glassSource]; every translucent panel on top calls [Modifier.glass],
 * which blurs whatever that source shows and tints it — matching the
 * `backdrop-filter: blur(...)` glass in the Claude Design canvas.
 */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

fun Modifier.glassSource(state: HazeState): Modifier = this.hazeSource(state)

@Composable
fun Modifier.glass(
    shape: Shape,
    tint: Color = PointTheme.colors.glass60,
    border: Color = PointTheme.colors.stroke80,
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 18.dp,
): Modifier {
    val state = LocalHazeState.current
    val filled = if (state != null) {
        // No backgroundColor override — let Haze blur the real content behind
        // (light gradient or camera) and only apply the translucent tint.
        this.clip(shape).hazeEffect(
            state = state,
            style = HazeStyle(
                tint = HazeTint(tint),
                blurRadius = blurRadius,
                noiseFactor = 0f,
            ),
        )
    } else {
        this.clip(shape).background(tint)
    }
    return filled.border(borderWidth, border, shape)
}

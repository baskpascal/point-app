package point.app.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The Claude Design canvas is authored at a fixed 393 px-wide phone frame. */
const val DESIGN_WIDTH = 393f

val LocalDesignScale = staticCompositionLocalOf { 1f }

/** Convert a canvas pixel value to a scaled [Dp] for the current screen width. */
@Composable
@ReadOnlyComposable
fun ds(px: Number): Dp = (px.toFloat() * LocalDesignScale.current).dp

/**
 * Hosts one screen at canvas proportions: scales the 393-wide coordinate system
 * to the real device width so every `ds(…)` value lands where the canvas puts it.
 */
@Composable
fun DesignScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val scale = maxWidth / DESIGN_WIDTH.dp
        CompositionLocalProvider(LocalDesignScale provides scale) {
            Box(Modifier.fillMaxSize(), content = content)
        }
    }
}

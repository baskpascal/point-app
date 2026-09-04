package point.app.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalPointColors = staticCompositionLocalOf { PointColors() }
private val LocalPointTypography = staticCompositionLocalOf<PointTypography> {
    error("PointTypography not provided — wrap content in PointTheme { }")
}
private val LocalPointGradients = staticCompositionLocalOf { PointGradients(PointColors()) }

/**
 * The Point design system entry point. Deliberately NOT `MaterialTheme` — Point
 * uses the Apple-like language from the Claude Design canvas, not Material.
 * Access tokens through [PointTheme]: `.colors`, `.type`, `.gradients`;
 * spacing/radii are plain objects ([PointSpacing], [PointRadii]).
 */
@Composable
fun PointTheme(
    colors: PointColors = PointColors(),
    content: @Composable () -> Unit,
) {
    val family = rememberUiFontFamily()
    val typography = remember(family) { PointTypography(family) }
    CompositionLocalProvider(
        LocalPointColors provides colors,
        LocalPointTypography provides typography,
        LocalPointGradients provides PointGradients(colors),
        content = content,
    )
}

object PointTheme {
    val colors: PointColors
        @Composable @ReadOnlyComposable get() = LocalPointColors.current

    val type: PointTypography
        @Composable @ReadOnlyComposable get() = LocalPointTypography.current

    val gradients: PointGradients
        @Composable @ReadOnlyComposable get() = LocalPointGradients.current
}

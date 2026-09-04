package point.app.ui.foundation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import point.app.design.PointTheme
import point.app.design.ds
import point.app.design.glowShadow

/** BasicText with an explicit color merged into the style. */
@Composable
fun PText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) = BasicText(
    text = text,
    modifier = modifier,
    style = style.merge(TextStyle(color = color, textAlign = align ?: TextAlign.Unspecified)),
    maxLines = maxLines,
)

/** Circular glass icon button (close / back). */
@Composable
fun CircleGlassButton(
    glyph: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = ds(44),
    tint: Color = PointTheme.colors.glass85,
    glyphColor: Color = PointTheme.colors.ink,
    glyphSize: TextStyle = PointTheme.type.title,
) {
    Box(
        modifier
            .size(diameter)
            .glass(CircleShape, tint = tint, border = PointTheme.colors.stroke90)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PText(glyph, glyphSize, glyphColor)
    }
}

/** Large coral→orange gradient CTA button. */
@Composable
fun GradientButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = ds(66),
    radius: Dp = ds(33),
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .glowShadow(shape)
            .clip(shape)
            .background(PointTheme.gradients.ctaWide)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PText(label, PointTheme.type.button, PointTheme.colors.white)
    }
}

/**
 * Listening meter driven by the real microphone [level] (0..1). At silence the
 * bars sit low and still; they rise with your voice. No fake idle animation.
 */
@Composable
fun LiveWaveform(
    level: Float,
    barColor: Color = PointTheme.colors.coralHot,
    barWidth: Dp = ds(4),
    gap: Dp = ds(5),
    maxHeight: Dp = ds(26),
) {
    val weights = listOf(0.55f, 0.9f, 0.7f, 1f, 0.65f, 0.85f)
    val smoothed by androidx.compose.animation.core.animateFloatAsState(
        targetValue = level.coerceIn(0f, 1f),
        animationSpec = tween(90),
        label = "micLevel",
    )
    Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
        weights.forEach { w ->
            val h = maxHeight * (0.18f + 0.82f * smoothed * w)
            Box(
                Modifier
                    .width(barWidth)
                    .height(h)
                    .clip(RoundedCornerShape(ds(2)))
                    .background(barColor),
            )
        }
    }
}

/** Coral vertical bars that pulse — the "listening" waveform. */
@Composable
fun Waveform(
    bars: List<Dp>,
    barColor: Color = PointTheme.colors.coralHot,
    barWidth: Dp = ds(4),
    gap: Dp = ds(5),
) {
    val transition = rememberInfiniteTransition()
    Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
        bars.forEachIndexed { i, h ->
            val scaleY by transition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, delayMillis = i * 100),
                    repeatMode = RepeatMode.Reverse,
                ),
            )
            Box(
                Modifier
                    .width(barWidth)
                    .height(h)
                    .scale(scaleX = 1f, scaleY = scaleY)
                    .clip(RoundedCornerShape(ds(2)))
                    .background(barColor),
            )
        }
    }
}

/** A small blinking dot. */
@Composable
fun BlinkingDot(color: Color, size: Dp, periodMillis: Int = 2400) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(periodMillis / 2), RepeatMode.Reverse),
    )
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha)),
    )
}

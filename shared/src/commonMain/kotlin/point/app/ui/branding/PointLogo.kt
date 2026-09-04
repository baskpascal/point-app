package point.app.ui.branding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import point.app.design.PointTheme
import point.app.design.ds

/**
 * The Point mark — a thick black ring with a coral→orange dot poking out the
 * top-right. Canvas: 100 px ring with a 25 px border, 33 px dot at (-6, -14).
 * Proportions are kept; [size] is the ring diameter.
 */
@Composable
fun PointLogo(size: Dp = ds(100)) {
    val ringBorder = size * 0.25f
    val dot = size * 0.33f
    Box(Modifier.size(size)) {
        Box(
            Modifier
                .size(size)
                .border(ringBorder, PointTheme.colors.inkStrong, CircleShape),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = size * 0.14f, y = -size * 0.06f)
                .size(dot)
                .clip(CircleShape)
                .background(PointTheme.gradients.accentDiagonal),
        )
    }
}

package point.app.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Corner radii — UI_SPEC.md §1.3 (`app/theme/radii.ts`). */
object PointRadii {
    val pill: Dp = 30.dp
    val card: Dp = 28.dp
    val buttonLarge: Dp = 32.dp
    val sm: Dp = 12.dp
    val md: Dp = 18.dp

    val pillShape = RoundedCornerShape(pill)
    val cardShape = RoundedCornerShape(card)
    val buttonShape = RoundedCornerShape(buttonLarge)
    val smShape = RoundedCornerShape(sm)
    val mdShape = RoundedCornerShape(md)
}

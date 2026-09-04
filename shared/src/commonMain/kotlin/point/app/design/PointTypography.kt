package point.app.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import point.shared.generated.resources.Inter_Bold
import point.shared.generated.resources.Inter_Medium
import point.shared.generated.resources.Inter_Regular
import point.shared.generated.resources.Res

/**
 * Inter — the shipped app font. The canvas (`Point Repair App.dc.html`) is
 * authored in DM Sans, but the user chose Inter for a macOS / San-Francisco
 * feel (SF Pro itself can't be bundled). This is the one deliberate deviation
 * from the canvas.
 */
@Composable
fun rememberUiFontFamily(): FontFamily = FontFamily(
    Font(Res.font.Inter_Regular, FontWeight.Normal),
    Font(Res.font.Inter_Medium, FontWeight.Medium),
    Font(Res.font.Inter_Bold, FontWeight.Bold),
)

/**
 * Type ramp — UI_SPEC.md §1.5, values from the Claude Design canvas.
 * px in the canvas maps 1:1 to sp here (the canvas frame is 393 wide).
 */
@Immutable
data class PointTypography(val family: FontFamily) {

    private fun s(size: Int, weight: FontWeight, lineHeight: Int = size, letterSpacingEm: Float = 0f) =
        TextStyle(
            fontFamily = family,
            fontSize = size.sp,
            lineHeight = lineHeight.sp,
            fontWeight = weight,
            letterSpacing = letterSpacingEm.em,
        )

    /** Splash wordmark "Point". */
    val wordmark = s(60, FontWeight.Medium, 60, -0.02f)

    /** Paywall headline "Fix with confidence." */
    val screenTitle = s(33, FontWeight.Bold, 38, -0.025f)

    /** Diagnosis "Worn chuck assembly". */
    val sectionTitle = s(27, FontWeight.Bold, 32, -0.02f)

    /** Confidence-ring number. */
    val metric = s(62, FontWeight.Bold, 62, -0.03f)
    val metricUnit = s(27, FontWeight.Medium, 27)

    /** Big gradient buttons ("Guide me", "Start free trial"). */
    val button = s(22, FontWeight.Medium, 26)

    /** Card titles, step titles, tool name. */
    val titleStrong = s(19, FontWeight.Bold, 24, -0.01f)
    val title = s(19, FontWeight.Medium, 24)

    /** Conversation bubbles, benefit rows, prompt text. */
    val body = s(18, FontWeight.Normal, 24)
    val bodyMedium = s(18, FontWeight.Medium, 24)

    /** Secondary lines. */
    val bodySm = s(17, FontWeight.Normal, 23)
    val label = s(16, FontWeight.Normal, 21)
    val caption = s(15, FontWeight.Normal, 20)
    val micro = s(13, FontWeight.Normal, 17)

    /** Splash tagline. */
    val tagline = s(19, FontWeight.Normal, 24)
}

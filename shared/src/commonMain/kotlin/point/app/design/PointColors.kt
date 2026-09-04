package point.app.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Point color tokens — extracted verbatim from the Claude Design canvas
 * (`Point Repair App.dc.html`), which is the visual source of truth. See
 * UI_SPEC.md §1.
 */
@Immutable
data class PointColors(
    // Surfaces
    val phone: Color = Color(0xFFF4F2F0),
    val cameraBg: Color = Color(0xFF1A1614),
    val guidanceBg: Color = Color(0xFF14100E),

    // Ink / text
    val inkStrong: Color = Color(0xFF0B0D10),
    val ink: Color = Color(0xFF12161B),
    val inkSoft: Color = Color(0xFF0E1216),
    val slate: Color = Color(0xFF5C6773),
    val slate2: Color = Color(0xFF7B8794),
    val slate3: Color = Color(0xFF8B96A2),

    // Accent — coral → orange
    val coral: Color = Color(0xFFF4425F),
    val coralDeep: Color = Color(0xFFF8544E),
    val coralHot: Color = Color(0xFFF4522F), // highlight ring / active timeline node
    val orange: Color = Color(0xFFFF9A3D),
    val orangeWarm: Color = Color(0xFFFFA23A),
    val coralHover: Color = Color(0xFFFF8A3D),
    val salmonSoft: Color = Color(0xFFF2836A), // "Save 40%"

    // Status
    val greenDot: Color = Color(0xFF2FBF5F),
    val greenCheck: Color = Color(0xFF3FAE62),
    val greenLabel: Color = Color(0xFF4AAB68),
    val greenBorder: Color = Color(0xFF9ADCAE),

    // Glass (white at alpha) — canvas uses .34–.95
    val glass34: Color = Color(0xFFFFFFFF).copy(alpha = 0.34f),
    val glass40: Color = Color(0xFFFFFFFF).copy(alpha = 0.40f),
    val glass45: Color = Color(0xFFFFFFFF).copy(alpha = 0.45f),
    val glass50: Color = Color(0xFFFFFFFF).copy(alpha = 0.50f),
    val glass55: Color = Color(0xFFFFFFFF).copy(alpha = 0.55f),
    val glass60: Color = Color(0xFFFFFFFF).copy(alpha = 0.60f),
    val glass72: Color = Color(0xFFFFFFFF).copy(alpha = 0.72f),
    val glass75: Color = Color(0xFFFFFFFF).copy(alpha = 0.75f),
    val glass78: Color = Color(0xFFFFFFFF).copy(alpha = 0.78f),
    val glass80: Color = Color(0xFFFFFFFF).copy(alpha = 0.80f),
    val glass85: Color = Color(0xFFFFFFFF).copy(alpha = 0.85f),
    val glass90: Color = Color(0xFFFFFFFF).copy(alpha = 0.90f),
    val glass93: Color = Color(0xFFFFFFFF).copy(alpha = 0.93f),

    val stroke45: Color = Color(0xFFFFFFFF).copy(alpha = 0.45f),
    val stroke80: Color = Color(0xFFFFFFFF).copy(alpha = 0.80f),
    val stroke85: Color = Color(0xFFFFFFFF).copy(alpha = 0.85f),
    val stroke90: Color = Color(0xFFFFFFFF).copy(alpha = 0.90f),
    val stroke95: Color = Color(0xFFFFFFFF).copy(alpha = 0.95f),

    // Hairlines
    val hairlineSoft: Color = Color(0x12000000), // rgba(0,0,0,.07)
    val hairline: Color = Color(0x24000000),     // rgba(0,0,0,.14)

    val white: Color = Color(0xFFFFFFFF),
    val black: Color = Color(0xFF000000),
)

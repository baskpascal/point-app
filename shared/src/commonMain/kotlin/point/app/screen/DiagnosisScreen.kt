package point.app.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import point.app.design.PointTheme
import point.app.design.ds
import point.app.ui.foundation.DiagnosisBackground
import point.app.ui.foundation.GradientButton
import point.app.ui.foundation.CircleGlassButton
import point.app.ui.foundation.LocalHazeState
import point.app.ui.foundation.PText
import point.app.ui.foundation.glass
import point.app.ui.foundation.glassSource

@Composable
fun DiagnosisScreen(onBack: () -> Unit, onGuide: () -> Unit, onNext: () -> Unit) {
    val c = PointTheme.colors
    val t = PointTheme.type
    val haze = remember { HazeState() }
    val dx = point.app.state.LocalRepairViewModel.current.state.collectAsState().value.diagnosis

    CompositionLocalProvider(LocalHazeState provides haze) {
        Box(Modifier.fillMaxSize().then(DiagnosisBackground()).glassSource(haze)) {

            CircleGlassButton("‹", onBack, Modifier.align(Alignment.TopStart).padding(start = ds(24), top = ds(24)), diameter = ds(42), tint = c.glass75)

            // Confidence ring
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = ds(92))
                    .size(ds(220))
                    .clip(CircleShape)
                    .glass(CircleShape, tint = c.glass60, border = c.stroke80),
                contentAlignment = Alignment.Center,
            ) {
                val ringTrack = c.hairlineSoft
                val sweepColors = listOf(c.coral, Color(0xFFF96B4A), c.orange, c.orange)
                val startAngle = 128f
                val sweepAngle = 320f * (dx.confidence / 100f)
                Canvas(Modifier.size(ds(184))) {
                    val stroke = size.minDimension * 0.078f
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val arcTopLeft = Offset(stroke / 2, stroke / 2)
                    // faint full track
                    drawArc(
                        color = ringTrack,
                        startAngle = 128f, sweepAngle = 320f, useCenter = false,
                        topLeft = arcTopLeft, size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                    // progress — rotate so the sweep gradient starts where the arc starts
                    rotate(degrees = startAngle, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(sweepColors, center = center),
                            startAngle = 0f, sweepAngle = sweepAngle, useCenter = false,
                            topLeft = arcTopLeft, size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                    }
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    PText("${dx.confidence}", t.metric, c.inkStrong)
                    PText("%", t.metricUnit, c.inkStrong, Modifier.padding(bottom = ds(6)))
                }
            }

            // Diagnosis card
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(start = ds(26), end = ds(26), top = ds(388))
                    .clip(RoundedCornerShape(ds(38)))
                    .glass(RoundedCornerShape(ds(38)), tint = c.glass72, border = c.stroke90)
                    .padding(start = ds(30), end = ds(30), top = ds(34), bottom = ds(30)),
            ) {
                PText(dx.label, t.label, c.slate3)
                Spacer(Modifier.height(ds(10)))
                PText(dx.headline.ifBlank { "Not diagnosed yet" }, t.sectionTitle, c.inkStrong)
                Spacer(Modifier.height(ds(14)))
                PText(
                    dx.supportingText.ifBlank { "Keep the camera on the tool and describe the problem — the assistant will diagnose it here." },
                    t.bodySm,
                    c.slate,
                )
                Spacer(Modifier.height(ds(26)))
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.hairline))
                Spacer(Modifier.height(ds(26)))
                Row(
                    Modifier.clickable(onClick = onNext),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ds(14)),
                ) {
                    Box(
                        Modifier.size(ds(38)).clip(CircleShape).background(c.glass90),
                        contentAlignment = Alignment.Center,
                    ) { PText("✦", t.bodySm, c.coralHot) }
                    PText("What to do next", t.bodySm.copy(fontWeight = FontWeight.Bold), c.coralHot)
                }
            }

            GradientButton(
                label = "Guide me",
                onClick = onGuide,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = ds(24), end = ds(24), bottom = ds(36)),
            )
        }
    }
}

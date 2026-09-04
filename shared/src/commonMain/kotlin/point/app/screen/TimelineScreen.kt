package point.app.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import point.app.design.PointTheme
import point.app.design.ds
import point.app.design.glowShadow
import point.app.ui.foundation.CircleGlassButton
import point.app.ui.foundation.LocalHazeState
import point.app.ui.foundation.PText
import point.app.ui.foundation.TimelineBackground
import point.app.ui.foundation.glass
import point.app.ui.foundation.glassSource
import point.app.state.LocalRepairViewModel
import point.app.state.StepStatus

@Composable
fun TimelineScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    val c = PointTheme.colors
    val t = PointTheme.type
    val haze = remember { HazeState() }
    val vm = LocalRepairViewModel.current
    val state by vm.state.collectAsState()
    val steps = state.steps

    CompositionLocalProvider(LocalHazeState provides haze) {
        Box(Modifier.fillMaxSize().then(TimelineBackground()).glassSource(haze)) {

            CircleGlassButton("‹", onBack, Modifier.align(Alignment.TopStart).padding(start = ds(26), top = ds(30)), tint = c.glass75)

            // Header card
            Row(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(start = ds(26), end = ds(24), top = ds(90))
                    .height(ds(110))
                    .glass(RoundedCornerShape(ds(26)), tint = c.glass45)
                    .padding(horizontal = ds(8)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ds(6)),
            ) {
                Box(
                    Modifier.size(ds(72)).clip(CircleShape).background(PointTheme.gradients.accentDiagonal),
                    contentAlignment = Alignment.Center,
                ) { PText("✦", t.sectionTitle, c.white) }
                Spacer(Modifier.width(ds(4)))
                Column(verticalArrangement = Arrangement.spacedBy(ds(4))) {
                    PText(state.tool.name.ifBlank { "No tool yet" }, t.titleStrong, c.ink)
                    PText(
                        if (steps.isEmpty()) "Point the camera and ask" else "Repair in progress",
                        t.label,
                        c.slate2,
                    )
                }
            }

            // Step list — scrolls between the header and the CTA, rows size to their text
            Column(
                Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(start = ds(34), end = ds(24), top = ds(220), bottom = ds(110))
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ds(6)),
            ) {
                if (steps.isEmpty()) {
                    PText("Steps appear here once the assistant maps out the repair.", t.body, c.slate2)
                }
                steps.forEachIndexed { i, step ->
                    StepRow(
                        number = i + 1,
                        title = step.label,
                        status = state.statusOf(i),
                        isLast = i == steps.lastIndex,
                    )
                }
            }

            // Continue CTA — glass, coral text
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = ds(32), end = ds(32), bottom = ds(34))
                    .height(ds(56))
                    .glowShadow(RoundedCornerShape(ds(28)))
                    .glass(RoundedCornerShape(ds(28)), tint = c.glass72, border = c.stroke95)
                    .clickable(onClick = onContinue),
                contentAlignment = Alignment.Center,
            ) {
                PText(
                    if (steps.isEmpty()) "Back to camera" else "Continue step ${state.activeStepNumber}",
                    t.title,
                    c.coralHot,
                )
            }
        }
    }
}

@Composable
private fun StepRow(number: Int, title: String, status: StepStatus, isLast: Boolean) {
    val c = PointTheme.colors
    val t = PointTheme.type
    val active = status == StepStatus.ACTIVE
    val highlight = if (active) {
        Modifier.drawBehind {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFFDCD4).copy(alpha = 0.8f), Color(0xFFFFECE6).copy(alpha = 0.15f)),
                ),
                topLeft = Offset(-14.dp.toPx(), -6.dp.toPx()),
                size = Size(size.width + 34.dp.toPx(), size.height + 12.dp.toPx()),
                cornerRadius = CornerRadius(26.dp.toPx()),
            )
        }
    } else Modifier

    Row(Modifier.heightIn(min = ds(56)).then(highlight)) {
        Column(
            Modifier.width(ds(34)).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (status) {
                StepStatus.COMPLETED -> Node(border = c.greenBorder, fill = c.white.copy(alpha = 0.85f)) {
                    PText("✓", t.caption, c.greenCheck)
                }
                StepStatus.ACTIVE -> Box(
                    Modifier.size(ds(34)).clip(CircleShape).background(c.white).border(ds(5), c.coralHot, CircleShape),
                )
                StepStatus.PENDING -> Node(border = c.hairline, fill = c.white.copy(alpha = 0.5f)) {}
            }
            if (!isLast) {
                Box(
                    Modifier
                        .width(ds(1.5f))
                        .weight(1f)
                        .background(
                            if (status == StepStatus.COMPLETED)
                                Brush.verticalGradient(listOf(c.greenBorder, c.coralHot))
                            else Brush.verticalGradient(listOf(c.hairline, c.hairline)),
                        ),
                )
            }
        }

        Spacer(Modifier.width(ds(22)))

        Column(
            Modifier.padding(top = ds(4), bottom = ds(4)).widthIn(max = ds(280)),
            verticalArrangement = Arrangement.spacedBy(ds(3)),
        ) {
            val labelColor = when (status) {
                StepStatus.COMPLETED -> c.greenLabel
                StepStatus.ACTIVE -> c.coralHot
                StepStatus.PENDING -> c.slate3
            }
            PText("Step $number", t.caption, labelColor)
            PText(
                title,
                if (active) t.titleStrong else t.title,
                if (status == StepStatus.PENDING) c.slate else c.ink,
            )
        }
    }
}

@Composable
private fun Node(border: Color, fill: Color, content: @Composable () -> Unit) {
    Box(
        Modifier.size(ds(34)).clip(CircleShape).background(fill).border(ds(1.5f), border, CircleShape),
        contentAlignment = Alignment.Center,
    ) { content() }
}

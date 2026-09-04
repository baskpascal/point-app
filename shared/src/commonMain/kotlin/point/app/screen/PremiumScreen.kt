package point.app.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import point.app.design.PointTheme
import point.app.design.ds
import point.app.ui.foundation.CircleGlassButton
import point.app.ui.foundation.GradientButton
import point.app.ui.foundation.LocalHazeState
import point.app.ui.foundation.PText
import point.app.ui.foundation.PremiumBackground
import point.app.ui.foundation.Waveform
import point.app.ui.foundation.glass
import point.app.ui.foundation.glassSource

@Composable
fun PremiumScreen(onClose: () -> Unit, onStartTrial: () -> Unit) {
    val c = PointTheme.colors
    val t = PointTheme.type
    val haze = remember { HazeState() }
    val vm = point.app.state.LocalRepairViewModel.current
    val state by vm.state.collectAsState()
    val yearly = state.selectedPlan == point.app.state.PlanId.YEARLY
    val benefits = state.premiumBenefits

    CompositionLocalProvider(LocalHazeState provides haze) {
        Box(Modifier.fillMaxSize().then(PremiumBackground()).glassSource(haze)) {

            CircleGlassButton(
                "✕", onClose,
                Modifier.align(Alignment.TopStart).padding(start = ds(26), top = ds(32)),
                tint = c.glass75,
            )

            Column(
                Modifier.fillMaxSize().padding(horizontal = ds(26)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(ds(96)))

                // Logo halo
                Box(Modifier.size(ds(150)), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(ds(150)).clip(CircleShape).background(
                            Brush.radialGradient(
                                listOf(
                                    c.white.copy(alpha = 0.9f),
                                    Color(0xFFFFD6BE).copy(alpha = 0.5f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                    )
                    Box(Modifier.size(ds(64))) {
                        Box(Modifier.size(ds(64)).border(ds(16), c.inkStrong, CircleShape))
                        Box(
                            Modifier.align(Alignment.TopEnd).size(ds(21)).clip(CircleShape)
                                .background(PointTheme.gradients.accentDiagonal),
                        )
                    }
                }

                Spacer(Modifier.height(ds(18)))
                PText("Fix with confidence.", t.screenTitle, c.inkStrong, align = TextAlign.Center)
                Spacer(Modifier.height(ds(12)))
                PText(
                    "Go beyond the basics with premium tools that help you repair smarter.",
                    t.label, c.slate, align = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = ds(6)),
                )

                Spacer(Modifier.height(ds(28)))

                // Benefits card
                val benefitIcons: List<@Composable () -> Unit> = listOf(
                    {
                        Waveform(
                            barColor = c.coralHot,
                            bars = listOf(ds(8), ds(13), ds(10), ds(6)),
                            barWidth = ds(2.5f), gap = ds(2.5f),
                        )
                    },
                    { PText("↺", t.title, c.coralHot) },
                    { PText("⌁", t.bodySm, c.coralHot) },
                )
                Column(
                    Modifier.fillMaxWidth()
                        .glass(RoundedCornerShape(ds(28)), tint = c.glass55, border = c.stroke85)
                        .padding(horizontal = ds(22)),
                ) {
                    benefits.take(3).forEachIndexed { i, b ->
                        if (i > 0) Hairline()
                        BenefitRow(benefitIcons[i.coerceAtMost(2)], b.label)
                    }
                }

                Spacer(Modifier.height(ds(22)))

                // Plan toggle
                Row(Modifier.fillMaxWidth().height(ds(78))) {
                    PlanPill(
                        Modifier.weight(1f),
                        RoundedCornerShape(topStart = ds(39), bottomStart = ds(39)),
                        selected = !yearly,
                        onClick = { vm.selectPlan(point.app.state.PlanId.MONTHLY) },
                    ) {
                        PText("Monthly", t.bodySm, c.ink)
                        PText("$5.99", t.bodySm, c.slate2)
                    }
                    Box(Modifier.weight(1.05f).fillMaxHeight()) {
                        PlanPill(
                            Modifier.fillMaxSize(),
                            RoundedCornerShape(ds(39)),
                            selected = yearly,
                            onClick = { vm.selectPlan(point.app.state.PlanId.YEARLY) },
                        ) {
                            PText("Yearly", t.bodySm, c.ink)
                            PText("$40.99", t.title, c.coral)
                            PText("Save 40%", t.micro, c.salmonSoft)
                        }
                        if (yearly) {
                            Box(
                                Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = ds(14))
                                    .size(ds(28))
                                    .clip(CircleShape)
                                    .background(PointTheme.gradients.accentDiagonal),
                                contentAlignment = Alignment.Center,
                            ) { PText("✓", t.caption, c.white) }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                GradientButton("Start free trial", onStartTrial, height = ds(64), radius = ds(32))
                Spacer(Modifier.height(ds(12)))
                PText("7 days free. Cancel anytime.", t.micro, c.slate3)
                Spacer(Modifier.height(ds(24)))
            }
        }
    }
}

@Composable
private fun BenefitRow(icon: @Composable () -> Unit, label: String) {
    val c = PointTheme.colors
    Row(
        Modifier.fillMaxWidth().height(ds(64)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ds(18)),
    ) {
        Box(
            Modifier.size(ds(40)).clip(CircleShape).background(c.glass90).border(1.dp, c.stroke95, CircleShape),
            contentAlignment = Alignment.Center,
        ) { icon() }
        PText(label, PointTheme.type.body, c.ink)
    }
}

@Composable
private fun Hairline() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(PointTheme.colors.hairlineSoft))
}

@Composable
private fun PlanPill(
    modifier: Modifier,
    shape: RoundedCornerShape,
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = PointTheme.colors
    Column(
        modifier
            .fillMaxHeight()
            .clip(shape)
            .background(if (selected) c.glass72 else c.glass45)
            .then(if (selected) Modifier.border(1.5.dp, c.orange.copy(alpha = 0.5f), shape) else Modifier)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

package point.app.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import point.app.design.PointTheme
import point.app.design.ds
import point.app.state.ConversationMessage
import point.app.state.Role
import point.app.ui.foundation.BlinkingDot
import point.app.ui.foundation.CircleGlassButton
import point.app.ui.foundation.GuidanceCameraBackground
import point.app.ui.foundation.LocalHazeState
import point.app.ui.foundation.LiveWaveform
import point.app.ui.foundation.PText
import point.app.ui.foundation.glass
import point.app.ui.foundation.glassSource
import point.app.ui.foundation.guidanceVignette

@Composable
fun GuidanceScreen(onBack: () -> Unit, onSteps: () -> Unit) {
    val c = PointTheme.colors
    val t = PointTheme.type
    val haze = remember { HazeState() }
    val vm = point.app.state.LocalRepairViewModel.current
    val state by vm.state.collectAsState()
    val messages = state.conversation.takeLast(5)
    val cam = point.app.session.LocalCameraController.current
    val perm by cam.permission.collectAsState()
    val ai = point.app.session.LocalAiLiveSession.current
    val micLevel by ai.micLevel.collectAsState()

    CompositionLocalProvider(LocalHazeState provides haze) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().glassSource(haze)) {
                Box(Modifier.matchParentSize().then(GuidanceCameraBackground()))
                if (perm == point.app.session.CameraPermission.GRANTED) {
                    point.app.session.CameraPreview(cam, Modifier.matchParentSize())
                }
                Box(Modifier.matchParentSize().guidanceVignette())
            }

            point.app.ui.foundation.FocusHighlight(state.focusBox, state.frameAspect, Modifier.matchParentSize())

            CircleGlassButton("←", onBack, Modifier.align(Alignment.TopStart).padding(start = ds(19), top = ds(26)), diameter = ds(46), tint = c.glass93)

            if (state.steps.isNotEmpty()) Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = ds(22), top = ds(30))
                    .height(ds(42))
                    .glass(RoundedCornerShape(ds(21)), tint = c.glass93)
                    .clickable(onClick = onSteps)
                    .padding(horizontal = ds(22)),
                contentAlignment = Alignment.Center,
            ) {
                PText("Step ${state.activeStepNumber} of ${state.steps.size}", t.bodySm, c.ink)
            }

            // Current action — the one thing to do right now
            if (state.instruction.isNotBlank()) Row(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = ds(22), end = ds(22), top = ds(92))
                    .fillMaxWidth()
                    .glass(RoundedCornerShape(ds(22)), tint = c.glass78, border = c.stroke45)
                    .padding(horizontal = ds(18), vertical = ds(16)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ds(14)),
            ) {
                Box(Modifier.size(ds(13)).border(ds(3.5f), c.coralHot, CircleShape))
                PText(state.instruction, t.titleStrong, c.inkStrong)
            }

            // Live conversation — translucent dark capsules over the camera, newest at the bottom
            val scroll = rememberScrollState()
            LaunchedEffect(messages.size, messages.lastOrNull()?.text) { scroll.animateScrollTo(scroll.maxValue) }
            if (messages.isNotEmpty()) Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = ds(18))
                    .padding(bottom = ds(96))
                    .heightIn(max = ds(300))
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(ds(10)),
            ) {
                messages.forEach { msg -> ConversationCapsule(msg) }
            }

            // Listening indicator — real mic level, only while the mic is engaged
            if (state.isListening) Row(
                Modifier.align(Alignment.BottomStart).padding(start = ds(28), bottom = ds(34)).height(ds(30)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ds(14)),
            ) {
                BlinkingDot(c.coralHot, ds(13), periodMillis = 1400)
                LiveWaveform(level = micLevel, barColor = c.coralHot)
            }
        }
    }
}

@Composable
private fun ConversationCapsule(msg: ConversationMessage) {
    val c = PointTheme.colors
    val t = PointTheme.type
    val assistant = msg.role == Role.ASSISTANT
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (assistant) Arrangement.Start else Arrangement.End,
    ) {
        Row(
            Modifier
                .widthIn(max = ds(300))
                .clip(RoundedCornerShape(ds(20)))
                .background(Color.Black.copy(alpha = 0.46f))
                .border(ds(1f), Color.White.copy(alpha = 0.12f), RoundedCornerShape(ds(20)))
                .padding(horizontal = ds(16), vertical = ds(12)),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(ds(10)),
        ) {
            if (assistant) PText("✦", t.title, c.orangeWarm)
            PText(
                text = msg.text,
                style = t.title,
                color = if (assistant) c.orangeWarm else Color.White,
            )
        }
    }
}

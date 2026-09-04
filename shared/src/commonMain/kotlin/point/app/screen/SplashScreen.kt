package point.app.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import point.app.design.PointTheme
import point.app.design.ds
import point.app.design.glowShadow
import point.app.ui.branding.PointLogo
import point.app.ui.foundation.PText
import point.app.ui.foundation.SplashBackground
import point.app.ui.foundation.glass
import kotlin.math.roundToInt

@Composable
fun SplashScreen(onStart: () -> Unit) {
    val c = PointTheme.colors
    val t = PointTheme.type

    Box(Modifier.fillMaxSize().then(SplashBackground())) {

        // Logo lockup — canvas anchors it at top:246 of the 851 frame (~29%).
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = ds(246)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PointLogo(ds(100))
            Spacer(Modifier.height(ds(14)))
            PText("Point", t.wordmark, c.inkStrong)
            Spacer(Modifier.height(ds(14)))
            PText("Point. Ask. Fix.", t.tagline, c.slate)
        }

        // Slide-to-start — drag the coral knob across the glass track to enter.
        SlideToStart(
            onStart = onStart,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = ds(45), end = ds(45), bottom = ds(154)),
        )
    }
}

@Composable
private fun SlideToStart(onStart: () -> Unit, modifier: Modifier = Modifier) {
    val c = PointTheme.colors
    val t = PointTheme.type
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val knob = ds(46)
    val pad = ds(10)
    val shape = RoundedCornerShape(ds(33))

    val knobPx = with(density) { knob.toPx() }
    val padPx = with(density) { pad.toPx() }
    val nudgePx = with(density) { ds(9).toPx() }

    var trackWidthPx by remember { mutableStateOf(0f) }
    val maxOffset = (trackWidthPx - knobPx - padPx * 2).coerceAtLeast(0f)

    val offsetX = remember { Animatable(0f) }
    var dragging by remember { mutableStateOf(false) }
    val progress = if (maxOffset > 0f) (offsetX.value / maxOffset).coerceIn(0f, 1f) else 0f

    // Idle affordance — a soft rightward nudge while untouched.
    androidx.compose.runtime.LaunchedEffect(dragging, maxOffset) {
        if (!dragging && maxOffset > 0f) {
            while (true) {
                delay(2400)
                if (offsetX.value != 0f) continue
                offsetX.animateTo(nudgePx, tween(360))
                offsetX.animateTo(0f, spring(stiffness = 260f, dampingRatio = 0.55f))
            }
        }
    }

    val hint = rememberInfiniteTransition(label = "slideHint")
    val hintAlpha by hint.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.44f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "hintAlpha",
    )

    Box(
        modifier
            .height(ds(66))
            .glowShadow(shape)
            .glass(shape, tint = c.glass60, border = c.stroke95)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
            .padding(horizontal = pad),
        contentAlignment = Alignment.CenterStart,
    ) {
        PText(
            "Slide to start",
            t.tagline,
            c.ink.copy(alpha = (1f - progress * 1.6f).coerceIn(0f, 1f)),
            Modifier.align(Alignment.Center),
        )
        PText(
            "›   ›   ›",
            t.title,
            c.slate.copy(alpha = hintAlpha * (1f - progress).coerceIn(0f, 1f)),
            Modifier
                .align(Alignment.CenterStart)
                .padding(start = knob + ds(14)),
        )

        Box(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(knob)
                .clip(CircleShape)
                .background(PointTheme.gradients.accentDiagonal)
                .pointerInput(maxOffset) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragging = true },
                        onDragEnd = {
                            dragging = false
                            if (maxOffset > 0f && offsetX.value >= maxOffset * 0.72f) {
                                scope.launch {
                                    offsetX.animateTo(maxOffset, tween(150))
                                    onStart()
                                }
                            } else {
                                scope.launch {
                                    offsetX.animateTo(0f, spring(stiffness = 420f, dampingRatio = 0.6f))
                                }
                            }
                        },
                        onDragCancel = {
                            dragging = false
                            scope.launch { offsetX.animateTo(0f, spring()) }
                        },
                    ) { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo((offsetX.value + dragAmount).coerceIn(0f, maxOffset))
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            PText("→", t.title, c.white)
        }
    }
}

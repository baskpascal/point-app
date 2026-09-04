package point.app.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import point.app.platform.BackNav
import point.app.design.DesignScreen
import point.app.nav.PointDestination
import point.app.nav.PointNavController
import point.app.nav.rememberPointNavController
import point.app.state.LocalRepairViewModel
import point.app.state.RepairViewModel

/** App root: owns the session view-model and the navigator. */
@Composable
fun PointApp() {
    val vm: RepairViewModel = viewModel { RepairViewModel() }
    val camera = point.app.session.rememberCameraController()
    val ai = point.app.session.rememberAiLiveSession()
    CompositionLocalProvider(
        LocalRepairViewModel provides vm,
        point.app.session.LocalCameraController provides camera,
        point.app.session.LocalAiLiveSession provides ai,
    ) {
        val nav = rememberPointNavController()
        PointNavHost(nav)
        point.app.session.RepairLiveController()
    }
}

@Composable
private fun PointNavHost(nav: PointNavController) {
    val vm = LocalRepairViewModel.current
    val ai = point.app.session.LocalAiLiveSession.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val state by vm.state.collectAsState()

    BackNav(enabled = nav.canPop) { nav.popBackStack() }

    // When the assistant lands a diagnosis while guiding, surface the diagnosis card
    // once (the user can go back to keep talking, or tap "Guide me" to continue).
    var diagnosisShown by remember { mutableStateOf(false) }
    LaunchedEffect(state.hasDiagnosis) {
        if (state.hasDiagnosis && !diagnosisShown && nav.current == PointDestination.LiveGuidance) {
            diagnosisShown = true
            nav.navigate(PointDestination.Diagnosis)
        }
        if (!state.hasDiagnosis) diagnosisShown = false
    }

    DesignScreen {
        AnimatedContent(
            targetState = nav.current,
            modifier = Modifier.fillMaxSize(),
            label = "nav",
            transitionSpec = {
                when {
                    targetState.isModal ->
                        slideInVertically(tween(320)) { it } togetherWith fadeOut(tween(200))
                    initialState.isModal ->
                        fadeIn(tween(200)) togetherWith slideOutVertically(tween(280)) { it }
                    else ->
                        fadeIn(tween(260)) togetherWith fadeOut(tween(180))
                }
            },
        ) { dest ->
            when (dest) {
                PointDestination.Splash -> SplashScreen(
                    onStart = {
                        // Splash is the entry point of every new repair session — clear
                        // whatever the previous session recognized/diagnosed/discussed
                        // so the AI doesn't carry it into this one.
                        vm.reset()
                        vm.setListening(true)
                        nav.navigate(PointDestination.LiveGuidance)
                    },
                )
                PointDestination.LiveCamera -> CameraScreen(
                    onClose = { nav.popBackStack() },
                    onAsk = {
                        vm.setListening(true)
                        nav.navigate(PointDestination.LiveGuidance)
                    },
                )
                PointDestination.LiveGuidance -> GuidanceScreen(
                    onBack = {
                        vm.setListening(false)
                        nav.popBackStack()
                    },
                    onSteps = { nav.navigate(PointDestination.StepTimeline) },
                )
                PointDestination.StepTimeline -> TimelineScreen(
                    onBack = { nav.popBackStack() },
                    onContinue = { nav.popBackStack() }, // back to the live guidance for this step
                )
                PointDestination.Diagnosis -> DiagnosisScreen(
                    onBack = { nav.popBackStack() },
                    onGuide = {
                        scope.launch {
                            runCatching { ai.sendText("I tapped Guide me — walk me through the fix one step at a time now.") }
                        }
                        nav.navigate(PointDestination.LiveGuidance)
                    },
                    onNext = { nav.navigate(PointDestination.Premium) },
                )
                PointDestination.Premium -> PremiumScreen(
                    onClose = { nav.popBackStack() },
                    onStartTrial = { nav.resetTo(PointDestination.Splash) },
                )
            }
        }
    }
}

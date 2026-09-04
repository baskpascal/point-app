package point.app.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import point.app.state.ConversationMessage
import point.app.state.LocalRepairViewModel
import point.app.state.Role

/**
 * Bridges the camera + Gemini Live session to the repair view-model. Mounted once
 * near the app root; it runs only while the user is "listening" (mic engaged).
 * Phase 8.
 */
@Composable
fun RepairLiveController() {
    val vm = LocalRepairViewModel.current
    val ai = LocalAiLiveSession.current
    val cam = LocalCameraController.current
    val listening by vm.state.collectAsState()

    // Route model events into the session state.
    LaunchedEffect(ai) {
        ai.events.collect { event ->
            when (event) {
                is AiEvent.InputTranscript ->
                    vm.appendTranscriptFragment(Role.USER, event.text)
                is AiEvent.OutputTranscript ->
                    vm.appendTranscriptFragment(Role.ASSISTANT, event.text)
                is AiEvent.RepairStateUpdate -> vm.applyAiPatch(event.patch)
                AiEvent.Interrupted, AiEvent.TurnComplete -> vm.finalizeStreamingTurn()
                AiEvent.Closed -> Unit
                is AiEvent.Error -> Unit
            }
        }
    }

    // Start/stop the live session with the mic toggle; stream ~1 fps frames while live.
    // Keyed on sessionEpoch too: reset() + setListening(true) can both land in one
    // synchronous update (isListening ends up true -> true), which a plain
    // isListening key would miss — sessionEpoch always changes, so this always
    // relaunches for a new repair session. The `finally` makes sure the *previous*
    // connection (if any) is actually torn down when that happens, not just the
    // local coroutine cancelled — NonCancellable because this runs during
    // cancellation cleanup, where a plain suspend call would be rejected instantly.
    LaunchedEffect(listening.isListening, listening.sessionEpoch) {
        if (listening.isListening) {
            try {
                ai.start()
                cam.frames.collect { frame ->
                    ai.sendVideoFrame(frame.jpeg)
                    if (frame.height > 0) vm.setFrameAspect(frame.width.toFloat() / frame.height)
                }
            } finally {
                kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) { ai.close() }
            }
        } else {
            ai.close()
        }
    }

    // Retire a stale focus highlight if the model stops updating it.
    LaunchedEffect(listening.focusBox) {
        if (listening.focusBox != null) {
            kotlinx.coroutines.delay(6000)
            vm.clearFocus()
        }
    }
}

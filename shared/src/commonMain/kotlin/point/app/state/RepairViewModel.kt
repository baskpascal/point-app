package point.app.state

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Single source of truth for the repair session. Starts [RepairSessionState.EMPTY]
 * and the Gemini Live session fills it in via [applyAiPatch]. [MockRepairSession]
 * is only for `@Preview`. Held as a [ViewModel] so it survives configuration changes.
 */
class RepairViewModel(initial: RepairSessionState = RepairSessionState.EMPTY) : ViewModel() {

    private val _state = MutableStateFlow(initial)
    val state: StateFlow<RepairSessionState> = _state.asStateFlow()

    fun setActiveStep(index: Int) = _state.update {
        if (it.steps.isEmpty()) it
        else it.copy(activeStepIndex = index.coerceIn(0, it.steps.lastIndex))
    }

    fun advanceStep() = _state.update {
        if (it.steps.isEmpty()) it
        else it.copy(activeStepIndex = (it.activeStepIndex + 1).coerceAtMost(it.steps.lastIndex))
    }

    fun setListening(listening: Boolean) = _state.update { it.copy(isListening = listening) }

    fun toggleListening() = _state.update { it.copy(isListening = !it.isListening) }

    fun selectPlan(plan: PlanId) = _state.update { it.copy(selectedPlan = plan) }

    fun appendMessage(message: ConversationMessage) = _state.update {
        it.copy(conversation = it.conversation + message)
    }

    /**
     * Live transcription arrives as small fragments. Extend the last message when
     * it's from the same speaker and this turn is still streaming; otherwise start
     * a new bubble. [finalizeStreamingTurn] closes the current turn.
     */
    fun appendTranscriptFragment(role: Role, fragment: String) = _state.update { s ->
        val last = s.conversation.lastOrNull()
        if (last != null && last.role == role && streamingRole == role) {
            s.copy(conversation = s.conversation.dropLast(1) + last.copy(text = (last.text + fragment).trimStart()))
        } else {
            val startingUserTurn = role == Role.USER
            streamingRole = role
            s.copy(
                conversation = s.conversation + ConversationMessage(role, fragment.trimStart()),
                focusBox = if (startingUserTurn) null else s.focusBox,
            )
        }
    }

    fun finalizeStreamingTurn() { streamingRole = null }

    fun clearFocus() = _state.update { if (it.focusBox == null) it else it.copy(focusBox = null) }

    fun setFrameAspect(aspect: Float) = _state.update {
        if (aspect <= 0f || kotlin.math.abs(it.frameAspect - aspect) < 0.01f) it else it.copy(frameAspect = aspect)
    }

    private var streamingRole: Role? = null

    /** Merge a partial update from the AI session (Phase 8). */
    fun applyPatch(patch: RepairSessionState.() -> RepairSessionState) = _state.update(patch)

    /** Apply a `set_repair_state` tool call from Gemini. */
    fun applyAiPatch(p: point.app.session.RepairStatePatch) = _state.update { s ->
        // The plan can only exist after a diagnosis — the model sometimes tries to
        // fabricate steps (or jump to "step 3") before diagnosing. Ignore those.
        val diagnosisNow = p.diagnosisHeadline?.isNotBlank() == true || s.hasDiagnosis
        val incomingSteps = if (diagnosisNow) p.steps else null
        val newSteps = incomingSteps?.mapIndexed { i, label -> RepairStep("step-${i + 1}", label) } ?: s.steps
        val boundedIndex = if (newSteps.isEmpty()) 0 else
            (p.activeStepIndex ?: s.activeStepIndex).coerceIn(0, newSteps.lastIndex)
        s.copy(
            tool = s.tool.copy(
                name = p.toolName ?: s.tool.name,
                recognizedLabel = p.recognizedLabel
                    ?: p.toolName?.let { "$it recognized" }
                    ?: s.tool.recognizedLabel,
            ),
            steps = newSteps,
            activeStepIndex = boundedIndex,
            instruction = p.instruction ?: s.instruction,
            diagnosis = s.diagnosis.copy(
                headline = p.diagnosisHeadline ?: s.diagnosis.headline,
                supportingText = p.diagnosisSupporting ?: s.diagnosis.supportingText,
                confidence = p.diagnosisConfidence ?: s.diagnosis.confidence,
                nextActions = p.diagnosisNextActions ?: s.diagnosis.nextActions,
            ),
            focusBox = p.focusBox?.takeIf { it.size == 4 }?.let {
                FocusBox(it[0] / 1000f, it[1] / 1000f, it[2] / 1000f, it[3] / 1000f)
            } ?: s.focusBox,
        )
    }
}

/** Provided once at the app root so any screen can read the session. */
val LocalRepairViewModel = compositionLocalOf<RepairViewModel> {
    error("RepairViewModel not provided — wrap content in PointApp()")
}

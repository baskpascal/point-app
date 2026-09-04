package point.app.state

import androidx.compose.runtime.Immutable

@Immutable
data class RepairStep(val id: String, val label: String)

enum class StepStatus { COMPLETED, ACTIVE, PENDING }

@Immutable
data class ToolRecognition(
    val name: String,
    val recognizedLabel: String,
    val thumbnailUrl: String? = null,
)

enum class Role { USER, ASSISTANT }

/**
 * Normalised bounding box of the part the assistant is currently pointing at,
 * in 0..1 fractions of the camera frame (top-left origin). Drawn as the live
 * "focus" highlight over the preview.
 */
@Immutable
data class FocusBox(val top: Float, val left: Float, val bottom: Float, val right: Float) {
    val width get() = (right - left).coerceIn(0f, 1f)
    val height get() = (bottom - top).coerceIn(0f, 1f)
}

@Immutable
data class ConversationMessage(val role: Role, val text: String)

@Immutable
data class Diagnosis(
    val confidence: Int,
    val label: String,
    val headline: String,
    val supportingText: String,
    val nextActions: List<String>,
)

enum class PlanId { MONTHLY, YEARLY }

@Immutable
data class PremiumBenefit(val label: String)

/**
 * The whole repair session, as one immutable snapshot. Screens render from this;
 * Phase 8 lets the Gemini Live session patch it. Mirrors the RN
 * `app/store/repairStore.ts` + `app/mocks/repairSession.ts`.
 */
@Immutable
data class RepairSessionState(
    val tool: ToolRecognition,
    val steps: List<RepairStep>,
    val activeStepIndex: Int,
    val instruction: String,
    val conversation: List<ConversationMessage>,
    val diagnosis: Diagnosis,
    val premiumBenefits: List<PremiumBenefit>,
    val isListening: Boolean,
    val selectedPlan: PlanId,
    val focusBox: FocusBox? = null,
    /** width/height of the camera frames the model is receiving — used to map its
     *  normalised focus box onto the centre-cropped preview. */
    val frameAspect: Float = 0.75f,
    /** Bumped by [point.app.state.RepairViewModel.reset]. isListening can go
     *  true -> reset -> true again within one synchronous update, which a StateFlow
     *  collector can conflate away; this gives RepairLiveController a key that's
     *  guaranteed to change so it actually restarts the AI session. */
    val sessionEpoch: Int = 0,
) {
    fun statusOf(index: Int): StepStatus = when {
        index < activeStepIndex -> StepStatus.COMPLETED
        index == activeStepIndex -> StepStatus.ACTIVE
        else -> StepStatus.PENDING
    }

    val activeStepNumber: Int get() = activeStepIndex + 1

    val hasTool: Boolean get() = tool.recognizedLabel.isNotBlank()
    val hasDiagnosis: Boolean get() = diagnosis.headline.isNotBlank()

    companion object {
        /** The real starting point: nothing recognised yet, the Gemini Live session fills it in. */
        val EMPTY = RepairSessionState(
            tool = ToolRecognition(name = "", recognizedLabel = ""),
            steps = emptyList(),
            activeStepIndex = 0,
            instruction = "",
            conversation = emptyList(),
            diagnosis = Diagnosis(0, "Diagnosis", "", "", emptyList()),
            premiumBenefits = listOf(
                PremiumBenefit("Unlimited live guidance"),
                PremiumBenefit("Repair history"),
                PremiumBenefit("Works across your tools"),
            ),
            isListening = false,
            selectedPlan = PlanId.YEARLY,
        )
    }
}

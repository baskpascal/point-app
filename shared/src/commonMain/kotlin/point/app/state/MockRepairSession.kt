package point.app.state

/** Seed data for the demo flow — port of RN `app/mocks/repairSession.ts`. */
object MockRepairSession {

    val initial = RepairSessionState(
        tool = ToolRecognition(
            name = "Makita DHP485",
            recognizedLabel = "Makita DHP485 recognized",
        ),
        steps = listOf(
            RepairStep("step-1", "Identify the model"),
            RepairStep("step-2", "Disconnect black cable"),
            RepairStep("step-3", "Open the housing"),
            RepairStep("step-4", "Inspect chuck assembly"),
            RepairStep("step-5", "Reassemble & test"),
        ),
        activeStepIndex = 1,
        instruction = "Disconnect the black cable on the left.",
        conversation = listOf(
            ConversationMessage(Role.USER, "Is this the cable?"),
            ConversationMessage(Role.ASSISTANT, "Yes — disconnect it gently."),
        ),
        diagnosis = Diagnosis(
            confidence = 91,
            label = "Diagnosis",
            headline = "Worn chuck assembly",
            supportingText = "The chuck no longer grips reliably.",
            nextActions = listOf(
                "Remove the chuck and inspect the jaws for wear.",
                "Clean debris from the collet threads.",
                "Replace the chuck if jaws no longer align evenly.",
            ),
        ),
        premiumBenefits = listOf(
            PremiumBenefit("Unlimited live guidance"),
            PremiumBenefit("Repair history"),
            PremiumBenefit("Works across your tools"),
        ),
        isListening = false,
        selectedPlan = PlanId.YEARLY,
    )
}

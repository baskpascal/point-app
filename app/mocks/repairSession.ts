// Mocked data to feed the UI for the MVP demo flow.
// Real recognition / diagnosis / guidance is Fase 7 (Future AI).

export type RepairStep = { id: string; label: string };

export const mockTool = {
  name: 'Makita DHP485',
  recognizedLabel: 'Makita DHP485 recognized',
  thumbnailUri: undefined as string | undefined,
};

export const mockSteps: RepairStep[] = [
  { id: 'step-1', label: 'Identify the model' },
  { id: 'step-2', label: 'Disconnect black cable' },
  { id: 'step-3', label: 'Open the housing' },
  { id: 'step-4', label: 'Inspect chuck assembly' },
  { id: 'step-5', label: 'Reassemble & test' },
];

export const mockActiveStepIndex = 1; // Step 2 of 5

export const mockConversation = [
  { role: 'user' as const, text: 'Is this the cable?' },
  { role: 'assistant' as const, text: 'Yes — disconnect it gently.' },
];

export const mockInstruction = 'Disconnect the black cable on the left.';

export const mockDiagnosis = {
  confidence: 91,
  label: 'Diagnosis',
  headline: 'Worn chuck assembly',
  supportingText: 'The chuck no longer grips reliably.',
  nextActions: [
    'Remove the chuck and inspect the jaws for wear.',
    'Clean debris from the collet threads.',
    'Replace the chuck if jaws no longer align evenly.',
  ],
};

export const mockPremiumBenefits = [
  'Unlimited live guidance',
  'Repair history',
  'Works across your tools',
];

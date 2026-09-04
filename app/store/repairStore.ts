import { create } from 'zustand';
import { mockActiveStepIndex, mockSteps, mockTool, RepairStep } from '@/mocks/repairSession';

type PlanId = 'monthly' | 'yearly';

type RepairState = {
  toolName: string;
  steps: RepairStep[];
  activeStepIndex: number;
  selectedPlan: PlanId;
  isListening: boolean;
  setActiveStepIndex: (index: number) => void;
  advanceStep: () => void;
  setListening: (listening: boolean) => void;
  setSelectedPlan: (plan: PlanId) => void;
};

export const useRepairStore = create<RepairState>((set, get) => ({
  toolName: mockTool.name,
  steps: mockSteps,
  activeStepIndex: mockActiveStepIndex,
  selectedPlan: 'yearly',
  isListening: false,
  setActiveStepIndex: (index) => set({ activeStepIndex: index }),
  advanceStep: () =>
    set({ activeStepIndex: Math.min(get().activeStepIndex + 1, get().steps.length - 1) }),
  setListening: (listening) => set({ isListening: listening }),
  setSelectedPlan: (plan) => set({ selectedPlan: plan }),
}));

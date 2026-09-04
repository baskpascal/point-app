import React from 'react';
import { GlassPill } from '@/components/foundation';

export function StepPill({ current, total }: { current: number; total: number }) {
  return <GlassPill tone="dark" label={`Step ${current} of ${total}`} />;
}

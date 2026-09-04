import React from 'react';
import { GlassBubble } from '@/components/foundation';

/** Main instructional line, e.g. "Disconnect the black cable on the left." */
export function InstructionBubble({ text }: { text: string }) {
  return <GlassBubble text={text} emphasis align="left" style={{ maxWidth: '92%' }} />;
}

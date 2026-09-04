import React from 'react';
import { View } from 'react-native';
import { GlassPill } from '@/components/foundation';
import { colors } from '@/theme';

/** Top status pill, e.g. "Makita DHP485 recognized". */
export function ObjectRecognizedPill({ label }: { label: string }) {
  return (
    <GlassPill
      tone="dark"
      label={label}
      icon={<View style={{ width: 8, height: 8, borderRadius: 4, backgroundColor: colors.successSoft, marginRight: 6 }} />}
    />
  );
}

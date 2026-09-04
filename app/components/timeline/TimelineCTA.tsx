import React from 'react';
import { View } from 'react-native';
import { GradientButton } from '@/components/foundation';

export function TimelineCTA({ label, onPress }: { label: string; onPress?: () => void }) {
  return (
    <View style={{ width: '100%' }}>
      <GradientButton label={label} onPress={onPress} />
    </View>
  );
}

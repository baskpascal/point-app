import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { GradientButton } from '@/components/foundation';
import { colors, typography } from '@/theme';

type Props = { onPress?: () => void; footer?: string };

export function TrialCTA({ onPress, footer = '7 days free. Cancel anytime.' }: Props) {
  return (
    <View style={styles.wrap}>
      <GradientButton label="Start free trial" onPress={onPress} />
      <Text style={styles.footer}>{footer}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { width: '100%', alignItems: 'center', gap: 10 },
  footer: { ...typography.caption, color: colors.textTertiary },
});

import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { PointLogo } from './PointLogo';
import { colors, spacing, typography } from '@/theme';

type Props = { tagline?: string; logoSize?: number };

/** Logo + wordmark + tagline stack used on Splash and Paywall. */
export function BrandLockup({ tagline = 'Point. Ask. Fix.', logoSize = 72 }: Props) {
  return (
    <View style={styles.wrap}>
      <PointLogo size={logoSize} />
      <Text style={styles.wordmark}>Point</Text>
      {tagline ? <Text style={styles.tagline}>{tagline}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { alignItems: 'center', gap: spacing.md },
  wordmark: { ...typography.screenTitle, color: colors.textPrimary, letterSpacing: -0.5 },
  tagline: { ...typography.body, color: colors.textSecondary },
});

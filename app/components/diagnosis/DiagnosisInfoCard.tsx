import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { GlassCard } from '@/components/foundation';
import { colors, spacing, typography } from '@/theme';

type Props = { label: string; headline: string; supportingText: string };

export function DiagnosisInfoCard({ label, headline, supportingText }: Props) {
  return (
    <GlassCard strong style={styles.card}>
      <Text style={styles.label}>{label}</Text>
      <Text style={styles.headline}>{headline}</Text>
      <Text style={styles.supporting}>{supportingText}</Text>
    </GlassCard>
  );
}

const styles = StyleSheet.create({
  card: { width: '100%' },
  label: { ...typography.caption, color: colors.accentCoral, fontWeight: '700', textTransform: 'uppercase', letterSpacing: 0.6 },
  headline: { ...typography.bodyLarge, color: colors.textPrimary, marginTop: spacing.sm, fontWeight: '700' },
  supporting: { ...typography.body, color: colors.textSecondary, marginTop: spacing.xs },
});

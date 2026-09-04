import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { GlassCard } from '@/components/foundation';
import { colors, spacing, typography } from '@/theme';

export function PremiumBenefitsCard({ benefits }: { benefits: string[] }) {
  return (
    <GlassCard strong style={styles.card}>
      {benefits.map((benefit, i) => (
        <View key={i} style={[styles.row, i > 0 && styles.rowSpacing]}>
          <View style={styles.check}>
            <Text style={styles.checkMark}>✓</Text>
          </View>
          <Text style={styles.text}>{benefit}</Text>
        </View>
      ))}
    </GlassCard>
  );
}

const styles = StyleSheet.create({
  card: { width: '100%' },
  row: { flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  rowSpacing: { marginTop: spacing.md },
  check: {
    width: 26,
    height: 26,
    borderRadius: 13,
    backgroundColor: colors.accentPeach,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkMark: { color: colors.accentCoral, fontWeight: '800', fontSize: 13 },
  text: { ...typography.body, color: colors.textPrimary, flex: 1 },
});

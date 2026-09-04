import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { colors, radii, spacing, typography } from '@/theme';

export type PlanId = 'monthly' | 'yearly';

type Plan = { id: PlanId; label: string; price: string; badge?: string };

const PLANS: Plan[] = [
  { id: 'monthly', label: 'Monthly', price: '$5.99/mo' },
  { id: 'yearly', label: 'Yearly', price: '$40.99/yr', badge: 'Save 40%' },
];

type Props = { selected: PlanId; onSelect: (plan: PlanId) => void };

export function PlanToggle({ selected, onSelect }: Props) {
  return (
    <View style={styles.wrap}>
      {PLANS.map((plan) => {
        const active = plan.id === selected;
        return (
          <TouchableOpacity
            key={plan.id}
            activeOpacity={0.85}
            onPress={() => onSelect(plan.id)}
            style={[styles.option, active && styles.optionActive]}
          >
            {plan.badge ? (
              <View style={styles.badge}>
                <Text style={styles.badgeText}>{plan.badge}</Text>
              </View>
            ) : null}
            <Text style={[styles.label, active && styles.labelActive]}>{plan.label}</Text>
            <Text style={[styles.price, active && styles.priceActive]}>{plan.price}</Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { flexDirection: 'row', gap: spacing.md, width: '100%' },
  option: {
    flex: 1,
    borderRadius: radii.md,
    borderWidth: 1.5,
    borderColor: 'rgba(16,24,40,0.08)',
    backgroundColor: colors.white,
    paddingVertical: spacing.lg,
    paddingHorizontal: spacing.md,
    alignItems: 'center',
  },
  optionActive: {
    borderColor: colors.accentCoral,
    backgroundColor: colors.accentPeach,
  },
  badge: {
    position: 'absolute',
    top: -10,
    backgroundColor: colors.accentCoral,
    borderRadius: radii.pill,
    paddingHorizontal: spacing.sm,
    paddingVertical: 2,
  },
  badgeText: { ...typography.caption, color: colors.white, fontWeight: '700', fontSize: 11 },
  label: { ...typography.bodyMedium, color: colors.textPrimary },
  labelActive: { color: colors.textPrimary },
  price: { ...typography.caption, color: colors.textSecondary, marginTop: 4 },
  priceActive: { color: colors.textPrimary },
});

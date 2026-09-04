import React from 'react';
import { SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '@/navigation/types';
import { AppBackground, IconCircleButton } from '@/components/foundation';
import { BrandLockup } from '@/components/branding';
import { PremiumBenefitsCard, PlanToggle, TrialCTA } from '@/components/paywall';
import { useRepairStore } from '@/store/repairStore';
import { mockPremiumBenefits } from '@/mocks/repairSession';
import { colors, spacing, typography } from '@/theme';

type Props = NativeStackScreenProps<RootStackParamList, 'Premium'>;

export function PremiumScreen({ navigation }: Props) {
  const selectedPlan = useRepairStore((s) => s.selectedPlan);
  const setSelectedPlan = useRepairStore((s) => s.setSelectedPlan);

  return (
    <AppBackground>
      <SafeAreaView style={styles.flex}>
        <View style={styles.topRow}>
          <View style={{ width: 44 }} />
          <IconCircleButton tone="light" onPress={() => navigation.goBack()}>
            <View style={styles.closeIcon} />
          </IconCircleButton>
        </View>

        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
          <BrandLockup tagline="" logoSize={56} />
          <View style={{ height: spacing.xl }} />
          <Text style={styles.headline}>Fix with confidence.</Text>
          <Text style={styles.subheadline}>
            Go beyond the basics with premium tools that help you repair smarter.
          </Text>

          <View style={{ height: spacing.xxxl }} />
          <PremiumBenefitsCard benefits={mockPremiumBenefits} />

          <View style={{ height: spacing.xxl }} />
          <PlanToggle selected={selectedPlan} onSelect={setSelectedPlan} />
        </ScrollView>

        <View style={styles.footer}>
          <TrialCTA onPress={() => navigation.goBack()} />
        </View>
      </SafeAreaView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  topRow: { flexDirection: 'row', justifyContent: 'space-between', paddingHorizontal: spacing.lg, paddingTop: spacing.sm },
  closeIcon: { width: 14, height: 2, backgroundColor: colors.textPrimary, borderRadius: 1, transform: [{ rotate: '45deg' }] },
  content: { paddingHorizontal: spacing.xl, alignItems: 'center', paddingBottom: spacing.xxxl },
  headline: { ...typography.screenTitle, color: colors.textPrimary, textAlign: 'center' },
  subheadline: {
    ...typography.body,
    color: colors.textSecondary,
    textAlign: 'center',
    marginTop: spacing.sm,
    paddingHorizontal: spacing.lg,
  },
  footer: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xl },
});

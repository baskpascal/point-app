import React from 'react';
import { SafeAreaView, ScrollView, StyleSheet, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '@/navigation/types';
import { AppBackground, IconCircleButton, GradientButton } from '@/components/foundation';
import { ConfidenceRing, DiagnosisInfoCard, NextActionSection } from '@/components/diagnosis';
import { mockDiagnosis } from '@/mocks/repairSession';
import { colors, spacing } from '@/theme';

type Props = NativeStackScreenProps<RootStackParamList, 'Diagnosis'>;

export function DiagnosisScreen({ navigation }: Props) {
  return (
    <AppBackground>
      <SafeAreaView style={styles.flex}>
        <View style={styles.topRow}>
          <IconCircleButton tone="light" onPress={() => navigation.goBack()}>
            <View style={styles.backIcon} />
          </IconCircleButton>
        </View>

        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
          <View style={styles.ringWrap}>
            <ConfidenceRing percent={mockDiagnosis.confidence} />
          </View>

          <View style={{ height: spacing.xxxl }} />

          <DiagnosisInfoCard
            label={mockDiagnosis.label}
            headline={mockDiagnosis.headline}
            supportingText={mockDiagnosis.supportingText}
          />

          <View style={{ height: spacing.xxxl }} />

          <NextActionSection items={mockDiagnosis.nextActions} />
        </ScrollView>

        <View style={styles.footer}>
          <GradientButton label="Guide me" onPress={() => navigation.navigate('LiveGuidance')} />
        </View>
      </SafeAreaView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  topRow: { paddingHorizontal: spacing.lg, paddingTop: spacing.sm },
  backIcon: {
    width: 10,
    height: 10,
    borderLeftWidth: 2,
    borderBottomWidth: 2,
    borderColor: colors.textPrimary,
    transform: [{ rotate: '45deg' }],
  },
  content: { paddingHorizontal: spacing.xl, paddingTop: spacing.xl, paddingBottom: spacing.xxxl, alignItems: 'center' },
  ringWrap: { alignItems: 'center', marginTop: spacing.lg },
  footer: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xl, alignItems: 'center' },
});

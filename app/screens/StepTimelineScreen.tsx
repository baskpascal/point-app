import React from 'react';
import { SafeAreaView, ScrollView, StyleSheet, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '@/navigation/types';
import { AppBackground, IconCircleButton } from '@/components/foundation';
import { RepairHeaderCard, TimelineList, TimelineCTA } from '@/components/timeline';
import { useRepairStore } from '@/store/repairStore';
import { mockTool } from '@/mocks/repairSession';
import { colors, spacing } from '@/theme';

type Props = NativeStackScreenProps<RootStackParamList, 'StepTimeline'>;

export function StepTimelineScreen({ navigation }: Props) {
  const steps = useRepairStore((s) => s.steps);
  const activeStepIndex = useRepairStore((s) => s.activeStepIndex);

  return (
    <AppBackground>
      <SafeAreaView style={styles.flex}>
        <View style={styles.topRow}>
          <IconCircleButton tone="light" onPress={() => navigation.goBack()}>
            <View style={styles.backIcon} />
          </IconCircleButton>
        </View>

        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
          <RepairHeaderCard
            title={mockTool.name}
            subtitle="Repair in progress"
            thumbnailUri={mockTool.thumbnailUri}
          />
          <View style={{ height: spacing.xxxl }} />
          <TimelineList steps={steps} activeIndex={activeStepIndex} />
        </ScrollView>

        <View style={styles.footer}>
          <TimelineCTA
            label={`Continue step ${activeStepIndex + 1}`}
            onPress={() => navigation.navigate('LiveGuidance')}
          />
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
  content: { paddingHorizontal: spacing.xl, paddingTop: spacing.xl, paddingBottom: spacing.xxxl },
  footer: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xl },
});

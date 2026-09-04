import React from 'react';
import { SafeAreaView, StyleSheet, TouchableOpacity, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '@/navigation/types';
import { IconCircleButton } from '@/components/foundation';
import { CameraPreview, ListeningWave } from '@/components/camera';
import { StepPill, HighlightOverlay, InstructionBubble, ConversationBubble } from '@/components/guidance';
import { useRepairStore } from '@/store/repairStore';
import { mockConversation, mockInstruction } from '@/mocks/repairSession';
import { colors, spacing } from '@/theme';
import { BlurView } from 'expo-blur';

type Props = NativeStackScreenProps<RootStackParamList, 'LiveGuidance'>;

export function LiveGuidanceScreen({ navigation }: Props) {
  const steps = useRepairStore((s) => s.steps);
  const activeStepIndex = useRepairStore((s) => s.activeStepIndex);

  return (
    <View style={styles.root}>
      <CameraPreview>
        <SafeAreaView style={styles.overlay}>
          <View style={styles.topRow}>
            <IconCircleButton onPress={() => navigation.goBack()}>
              <View style={styles.backIcon} />
            </IconCircleButton>
            <StepPill current={activeStepIndex + 1} total={steps.length} />
            <TouchableOpacity onPress={() => navigation.navigate('StepTimeline')}>
              <View style={{ width: 44 }} />
            </TouchableOpacity>
          </View>

          <View style={styles.center}>
            <HighlightOverlay />
          </View>

          <View style={styles.conversation}>
            <InstructionBubble text={mockInstruction} />
            <View style={{ height: spacing.sm }} />
            {mockConversation.map((msg, i) => (
              <View key={i} style={{ marginTop: spacing.sm }}>
                <ConversationBubble role={msg.role} text={msg.text} />
              </View>
            ))}
          </View>

          <View style={styles.listeningBar}>
            <BlurView intensity={26} tint="dark" style={StyleSheet.absoluteFill} />
            <View style={styles.listeningTint} />
            <ListeningWave size="sm" />
          </View>
        </SafeAreaView>
      </CameraPreview>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#000' },
  overlay: { flex: 1, justifyContent: 'space-between', paddingHorizontal: spacing.lg, paddingBottom: spacing.lg },
  topRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: spacing.sm },
  backIcon: { width: 10, height: 10, borderLeftWidth: 2, borderBottomWidth: 2, borderColor: colors.white, transform: [{ rotate: '45deg' }] },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  conversation: { marginBottom: spacing.lg },
  listeningBar: {
    alignSelf: 'center',
    borderRadius: 24,
    overflow: 'hidden',
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.xl,
  },
  listeningTint: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(16,24,40,0.30)',
    borderRadius: 24,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.18)',
  },
});

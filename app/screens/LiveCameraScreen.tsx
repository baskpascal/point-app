import React from 'react';
import { SafeAreaView, StyleSheet, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '@/navigation/types';
import { IconCircleButton } from '@/components/foundation';
import {
  CameraPreview,
  RecognitionFrame,
  ObjectRecognizedPill,
  LiveCameraBadge,
  VoiceInputDock,
} from '@/components/camera';
import { mockTool } from '@/mocks/repairSession';
import { useRepairStore } from '@/store/repairStore';
import { spacing } from '@/theme';

type Props = NativeStackScreenProps<RootStackParamList, 'LiveCamera'>;

export function LiveCameraScreen({ navigation }: Props) {
  const isListening = useRepairStore((s) => s.isListening);
  const setListening = useRepairStore((s) => s.setListening);

  const handleMicPress = () => {
    setListening(!isListening);
    // Mocked: after a beat, move into guided repair.
    navigation.navigate('LiveGuidance');
  };

  return (
    <View style={styles.root}>
      <CameraPreview>
        <SafeAreaView style={styles.overlay}>
          <View style={styles.topRow}>
            <IconCircleButton onPress={() => navigation.goBack()}>
              <View style={styles.closeIcon} />
            </IconCircleButton>
            <ObjectRecognizedPill label={mockTool.recognizedLabel} />
            <View style={{ width: 44 }} />
          </View>

          <View style={styles.center}>
            <RecognitionFrame />
          </View>

          <View style={styles.bottom}>
            <LiveCameraBadge />
            <View style={{ height: spacing.md }} />
            <VoiceInputDock listening={isListening} onPressMic={handleMicPress} />
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
  closeIcon: { width: 14, height: 2, backgroundColor: '#fff', borderRadius: 1 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  bottom: {},
});

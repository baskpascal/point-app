import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { BlurView } from 'expo-blur';
import { LinearGradient } from 'expo-linear-gradient';
import * as Haptics from 'expo-haptics';
import { colors, gradients, radii, spacing, typography } from '@/theme';

type Props = {
  prompt?: string;
  onPressMic?: () => void;
  listening?: boolean;
};

/** Bottom dock: "What do you want to fix?" + gradient mic button. */
export function VoiceInputDock({ prompt = 'What do you want to fix?', onPressMic, listening }: Props) {
  const handlePress = () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium).catch(() => {});
    onPressMic?.();
  };

  return (
    <View style={styles.wrap}>
      <BlurView intensity={26} tint="dark" style={StyleSheet.absoluteFill} />
      <View style={[StyleSheet.absoluteFill, styles.tint]} />
      <Text style={styles.prompt}>{prompt}</Text>
      <TouchableOpacity activeOpacity={0.85} onPress={handlePress}>
        <LinearGradient colors={gradients.cta} style={[styles.mic, listening && styles.micActive]}>
          <View style={styles.micIcon} />
        </LinearGradient>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    borderRadius: radii.buttonLarge,
    overflow: 'hidden',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.xl,
  },
  tint: {
    backgroundColor: 'rgba(16,24,40,0.30)',
    borderRadius: radii.buttonLarge,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.18)',
  },
  prompt: { ...typography.body, color: colors.white, flex: 1, marginRight: spacing.md },
  mic: {
    width: 52,
    height: 52,
    borderRadius: 26,
    alignItems: 'center',
    justifyContent: 'center',
  },
  micActive: { transform: [{ scale: 1.05 }] },
  micIcon: { width: 16, height: 16, borderRadius: 8, backgroundColor: colors.white },
});

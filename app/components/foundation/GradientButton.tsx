import React from 'react';
import {
  ActivityIndicator,
  GestureResponderEvent,
  StyleSheet,
  Text,
  TouchableOpacity,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import * as Haptics from 'expo-haptics';
import { colors, gradients, radii, spacing, typography } from '@/theme';

type Props = {
  label: string;
  onPress?: (e: GestureResponderEvent) => void;
  icon?: React.ReactNode;
  loading?: boolean;
  disabled?: boolean;
  size?: 'large' | 'medium';
};

/** Primary coral→orange gradient CTA used across all screens. */
export function GradientButton({
  label,
  onPress,
  icon,
  loading,
  disabled,
  size = 'large',
}: Props) {
  const handlePress = (e: GestureResponderEvent) => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium).catch(() => {});
    onPress?.(e);
  };

  return (
    <TouchableOpacity
      activeOpacity={0.85}
      disabled={disabled || loading}
      onPress={handlePress}
      style={disabled ? styles.disabled : undefined}
    >
      <LinearGradient
        colors={gradients.cta}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        style={[styles.button, size === 'medium' && styles.buttonMedium]}
      >
        {loading ? (
          <ActivityIndicator color={colors.white} />
        ) : (
          <>
            {icon}
            <Text style={styles.label}>{label}</Text>
          </>
        )}
      </LinearGradient>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  button: {
    borderRadius: radii.buttonLarge,
    paddingVertical: spacing.lg + 2,
    paddingHorizontal: spacing.xxxl,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.sm,
  },
  buttonMedium: {
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.xxl,
  },
  label: { ...typography.bodyMedium, color: colors.white, fontWeight: '700' },
  disabled: { opacity: 0.5 },
});

import React from 'react';
import { StyleSheet, View, ViewProps } from 'react-native';
import { BlurView } from 'expo-blur';
import { colors, radii, shadows } from '@/theme';

type Props = ViewProps & {
  /** blur intensity, 0-100. Spec range: 18-28 for light surfaces. */
  intensity?: number;
  strong?: boolean;
  radius?: number;
};

/**
 * The base frosted-glass surface used across the app: translucent,
 * blurred, soft white inner border, never opaque/"milky".
 */
export function GlassCard({
  children,
  style,
  intensity = 24,
  strong = false,
  radius = radii.card,
  ...rest
}: Props) {
  return (
    <View style={[{ borderRadius: radius, overflow: 'hidden' }, styles.shadowWrap, style]} {...rest}>
      <BlurView intensity={intensity} tint="light" style={StyleSheet.absoluteFill} />
      <View
        style={[
          StyleSheet.absoluteFill,
          {
            backgroundColor: strong ? colors.surfaceGlassStrong : colors.surfaceGlass,
            borderRadius: radius,
            borderWidth: 1,
            borderColor: colors.strokeGlass,
          },
        ]}
      />
      <View style={styles.content}>{children}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  shadowWrap: { ...(shadows.soft as object) },
  content: { padding: 20 },
});

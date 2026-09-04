import React from 'react';
import { StyleSheet, Text, View, ViewProps } from 'react-native';
import { BlurView } from 'expo-blur';
import { colors, radii, spacing, typography } from '@/theme';

type Props = ViewProps & {
  text: string;
  align?: 'left' | 'right';
  emphasis?: boolean;
};

/**
 * Floating frosted-glass bubble for conversation over the camera feed.
 * Deliberately NOT a traditional messaging bubble: no big solid color
 * blocks, no tails — just light glass, rounded, floating.
 */
export function GlassBubble({ text, align = 'left', emphasis = false, style, ...rest }: Props) {
  return (
    <View
      style={[styles.wrap, align === 'right' ? styles.alignRight : styles.alignLeft, style]}
      {...rest}
    >
      <BlurView intensity={emphasis ? 30 : 22} tint="dark" style={StyleSheet.absoluteFill} />
      <View
        style={[
          StyleSheet.absoluteFill,
          {
            backgroundColor: emphasis ? 'rgba(16,24,40,0.42)' : 'rgba(16,24,40,0.30)',
            borderRadius: radii.md,
            borderWidth: 1,
            borderColor: 'rgba(255,255,255,0.18)',
          },
        ]}
      />
      <Text style={styles.text}>{text}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    borderRadius: radii.md,
    overflow: 'hidden',
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.lg,
    maxWidth: '86%',
  },
  alignLeft: { alignSelf: 'flex-start' },
  alignRight: { alignSelf: 'flex-end' },
  text: { ...typography.body, color: colors.white },
});

import React from 'react';
import { StyleSheet, Text, View, ViewProps } from 'react-native';
import { BlurView } from 'expo-blur';
import { colors, radii, spacing, typography } from '@/theme';

type Props = ViewProps & {
  label: string;
  icon?: React.ReactNode;
  tone?: 'default' | 'dark';
};

/** Small translucent capsule used for status/step badges over the camera. */
export function GlassPill({ label, icon, tone = 'default', style, ...rest }: Props) {
  const dark = tone === 'dark';
  return (
    <View style={[styles.wrap, style]} {...rest}>
      <BlurView intensity={dark ? 32 : 22} tint={dark ? 'dark' : 'light'} style={StyleSheet.absoluteFill} />
      <View
        style={[
          StyleSheet.absoluteFill,
          {
            backgroundColor: dark ? 'rgba(16,24,40,0.38)' : colors.surfaceGlass,
            borderRadius: radii.pill,
            borderWidth: 1,
            borderColor: dark ? 'rgba(255,255,255,0.18)' : colors.strokeGlass,
          },
        ]}
      />
      <View style={styles.row}>
        {icon}
        <Text style={[styles.label, { color: dark ? colors.white : colors.textPrimary }]}>{label}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    borderRadius: radii.pill,
    overflow: 'hidden',
    alignSelf: 'flex-start',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.lg,
  },
  label: { ...typography.caption, fontWeight: '600' },
});

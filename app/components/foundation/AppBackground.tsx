import React from 'react';
import { StyleSheet, View, ViewProps } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { colors } from '@/theme';

/**
 * Global light background used by every non-camera screen:
 * base off-white + two very soft ambient glows (coral bottom-left,
 * cool bottom-right), low intensity — never a hard gradient.
 */
export function AppBackground({ children, style, ...rest }: ViewProps) {
  return (
    <View style={[styles.root, style]} {...rest}>
      <LinearGradient
        colors={[colors.bgPrimary, colors.bgSecondary]}
        style={StyleSheet.absoluteFill}
      />
      <LinearGradient
        colors={['rgba(255,111,108,0.16)', 'rgba(255,111,108,0)']}
        start={{ x: 0, y: 1 }}
        end={{ x: 0.6, y: 0.4 }}
        style={[StyleSheet.absoluteFill, styles.glowBottomLeft]}
      />
      <LinearGradient
        colors={['rgba(140,160,200,0.12)', 'rgba(140,160,200,0)']}
        start={{ x: 1, y: 1 }}
        end={{ x: 0.4, y: 0.4 }}
        style={[StyleSheet.absoluteFill, styles.glowBottomRight]}
      />
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: colors.bgPrimary },
  glowBottomLeft: { opacity: 0.9 },
  glowBottomRight: { opacity: 0.9 },
});

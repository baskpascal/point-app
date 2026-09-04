import React, { useEffect } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import Animated, { useAnimatedStyle, useSharedValue, withRepeat, withTiming } from 'react-native-reanimated';
import { colors, radii, spacing, typography } from '@/theme';

/** Tiny "Live Camera" badge with a pulsing red dot. */
export function LiveCameraBadge() {
  const pulse = useSharedValue(1);

  useEffect(() => {
    pulse.value = withRepeat(withTiming(0.4, { duration: 700 }), -1, true);
  }, []);

  const dotStyle = useAnimatedStyle(() => ({ opacity: pulse.value }));

  return (
    <View style={styles.wrap}>
      <Animated.View style={[styles.dot, dotStyle]} />
      <Text style={styles.label}>Live Camera</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    backgroundColor: 'rgba(16,24,40,0.32)',
    borderRadius: radii.pill,
    paddingVertical: 6,
    paddingHorizontal: spacing.md,
    alignSelf: 'flex-start',
  },
  dot: { width: 6, height: 6, borderRadius: 3, backgroundColor: '#FF5A5A' },
  label: { ...typography.caption, color: colors.white, fontWeight: '600' },
});

import React, { useEffect } from 'react';
import { StyleSheet, View, ViewStyle } from 'react-native';
import Animated, { useAnimatedStyle, useSharedValue, withRepeat, withTiming } from 'react-native-reanimated';
import { colors } from '@/theme';

type Props = { style?: ViewStyle; size?: number };

/** Soft coral glow ring around the relevant part of the object — never a solid block. */
export function HighlightOverlay({ style, size = 160 }: Props) {
  const glow = useSharedValue(0.4);

  useEffect(() => {
    glow.value = withRepeat(withTiming(1, { duration: 1100 }), -1, true);
  }, []);

  const animatedStyle = useAnimatedStyle(() => ({ opacity: glow.value }));

  return (
    <View style={[styles.wrap, { width: size, height: size }, style]}>
      <Animated.View style={[styles.ring, animatedStyle]} />
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { alignItems: 'center', justifyContent: 'center' },
  ring: {
    width: '100%',
    height: '100%',
    borderRadius: 999,
    borderWidth: 3,
    borderColor: colors.accentCoral,
  },
});

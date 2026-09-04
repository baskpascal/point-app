import React, { useEffect } from 'react';
import { StyleSheet, View } from 'react-native';
import Animated, {
  useAnimatedStyle,
  useSharedValue,
  withRepeat,
  withTiming,
  Easing,
} from 'react-native-reanimated';
import { colors } from '@/theme';

type Props = { width?: number; height?: number };

const CORNER = 28;

/** Soft rounded frame with animated corner accents over the recognized object. */
export function RecognitionFrame({ width = 220, height = 220 }: Props) {
  const pulse = useSharedValue(0.6);

  useEffect(() => {
    pulse.value = withRepeat(withTiming(1, { duration: 1400, easing: Easing.inOut(Easing.ease) }), -1, true);
  }, []);

  const animatedStyle = useAnimatedStyle(() => ({ opacity: pulse.value }));

  return (
    <View style={[styles.wrap, { width, height }]}>
      <Animated.View style={[styles.corner, styles.topLeft, animatedStyle]} />
      <Animated.View style={[styles.corner, styles.topRight, animatedStyle]} />
      <Animated.View style={[styles.corner, styles.bottomLeft, animatedStyle]} />
      <Animated.View style={[styles.corner, styles.bottomRight, animatedStyle]} />
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { alignSelf: 'center' },
  corner: {
    position: 'absolute',
    width: CORNER,
    height: CORNER,
    borderColor: colors.accentPeach,
  },
  topLeft: { top: 0, left: 0, borderTopWidth: 3, borderLeftWidth: 3, borderTopLeftRadius: 12 },
  topRight: { top: 0, right: 0, borderTopWidth: 3, borderRightWidth: 3, borderTopRightRadius: 12 },
  bottomLeft: { bottom: 0, left: 0, borderBottomWidth: 3, borderLeftWidth: 3, borderBottomLeftRadius: 12 },
  bottomRight: { bottom: 0, right: 0, borderBottomWidth: 3, borderRightWidth: 3, borderBottomRightRadius: 12 },
});

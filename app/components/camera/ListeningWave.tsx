import React, { useEffect } from 'react';
import { StyleSheet, View } from 'react-native';
import Animated, { useAnimatedStyle, useSharedValue, withDelay, withRepeat, withSequence, withTiming } from 'react-native-reanimated';
import { gradients } from '@/theme';
import { LinearGradient } from 'expo-linear-gradient';

const BARS = 5;

/** Small animated waveform used inside the voice dock / listening bar. */
export function ListeningWave({ active = true, size = 'md' }: { active?: boolean; size?: 'sm' | 'md' }) {
  const barHeight = size === 'sm' ? 14 : 22;

  return (
    <View style={[styles.row, { height: barHeight }]}>
      {Array.from({ length: BARS }).map((_, i) => (
        <Bar key={i} index={i} active={active} maxHeight={barHeight} />
      ))}
    </View>
  );
}

function Bar({ index, active, maxHeight }: { index: number; active: boolean; maxHeight: number }) {
  const scale = useSharedValue(0.3);

  useEffect(() => {
    if (!active) {
      scale.value = withTiming(0.3);
      return;
    }
    scale.value = withDelay(
      index * 90,
      withRepeat(
        withSequence(
          withTiming(1, { duration: 320 }),
          withTiming(0.35, { duration: 320 }),
        ),
        -1,
        true,
      ),
    );
  }, [active]);

  const style = useAnimatedStyle(() => ({ transform: [{ scaleY: scale.value }] }));

  return (
    <Animated.View style={[styles.barWrap, { height: maxHeight }, style]}>
      <LinearGradient colors={gradients.cta} style={styles.bar} start={{ x: 0, y: 0 }} end={{ x: 0, y: 1 }} />
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  barWrap: { width: 4, justifyContent: 'flex-end' },
  bar: { flex: 1, borderRadius: 2 },
});

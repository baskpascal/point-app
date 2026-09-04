import React from 'react';
import { gradients } from '@/theme';
import { LinearGradient } from 'expo-linear-gradient';
import { StyleSheet, View } from 'react-native';

type Props = { size?: number };

/** Minimal geometric mark: a coral→orange "pin/point" dot in a ring. */
export function PointLogo({ size = 72 }: Props) {
  return (
    <View style={{ width: size, height: size }}>
      <LinearGradient
        colors={gradients.cta}
        style={[styles.ring, { width: size, height: size, borderRadius: size / 2 }]}
      >
        <View
          style={[
            styles.inner,
            {
              width: size * 0.42,
              height: size * 0.42,
              borderRadius: (size * 0.42) / 2,
            },
          ]}
        />
      </LinearGradient>
    </View>
  );
}

const styles = StyleSheet.create({
  ring: { alignItems: 'center', justifyContent: 'center' },
  inner: { backgroundColor: '#FFFFFF' },
});

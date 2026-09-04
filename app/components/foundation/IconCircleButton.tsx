import React from 'react';
import { GestureResponderEvent, StyleSheet, TouchableOpacity, View, ViewStyle } from 'react-native';
import { BlurView } from 'expo-blur';
import { colors } from '@/theme';

type Props = {
  children: React.ReactNode;
  onPress?: (e: GestureResponderEvent) => void;
  size?: number;
  tone?: 'light' | 'dark';
  style?: ViewStyle;
};

/** Circular glass icon button — used for close/back buttons over camera & light bg. */
export function IconCircleButton({ children, onPress, size = 44, tone = 'dark', style }: Props) {
  const dark = tone === 'dark';
  return (
    <TouchableOpacity
      activeOpacity={0.75}
      onPress={onPress}
      style={[
        {
          width: size,
          height: size,
          borderRadius: size / 2,
          overflow: 'hidden',
          alignItems: 'center',
          justifyContent: 'center',
        },
        style,
      ]}
    >
      <BlurView intensity={dark ? 30 : 20} tint={dark ? 'dark' : 'light'} style={StyleSheet.absoluteFill} />
      <View
        style={[
          StyleSheet.absoluteFill,
          {
            backgroundColor: dark ? 'rgba(16,24,40,0.35)' : colors.surfaceGlass,
            borderRadius: size / 2,
            borderWidth: 1,
            borderColor: dark ? 'rgba(255,255,255,0.18)' : colors.strokeGlass,
          },
        ]}
      />
      {children}
    </TouchableOpacity>
  );
}

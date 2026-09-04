import React, { useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { colors, typography } from '@/theme';
import { Text } from 'react-native';

/**
 * Full-screen live camera. Falls back to a dark placeholder (still
 * showing all overlays) when permission isn't granted yet — the demo
 * flow must never be blocked by a permission dialog.
 */
export function CameraPreview({ children }: { children?: React.ReactNode }) {
  const [permission, requestPermission] = useCameraPermissions();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!permission) return;
    if (!permission.granted) requestPermission();
    else setReady(true);
  }, [permission]);

  return (
    <View style={StyleSheet.absoluteFill}>
      {ready ? (
        <CameraView style={StyleSheet.absoluteFill} facing="back" />
      ) : (
        <View style={[StyleSheet.absoluteFill, styles.fallback]}>
          <Text style={styles.fallbackText}>Point your camera at the tool</Text>
        </View>
      )}
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  fallback: {
    backgroundColor: '#12151A',
    alignItems: 'center',
    justifyContent: 'center',
  },
  fallbackText: { ...typography.body, color: colors.white, opacity: 0.6 },
});

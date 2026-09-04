import React from 'react';
import { StyleSheet, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '@/navigation/types';
import { AppBackground, GradientButton } from '@/components/foundation';
import { BrandLockup } from '@/components/branding';
import { spacing } from '@/theme';

type Props = NativeStackScreenProps<RootStackParamList, 'Splash'>;

export function SplashScreen({ navigation }: Props) {
  return (
    <AppBackground>
      <View style={styles.center}>
        <BrandLockup tagline="Point. Ask. Fix." logoSize={84} />
      </View>
      <View style={styles.cta}>
        <GradientButton label="Start a repair" onPress={() => navigation.navigate('LiveCamera')} />
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  cta: { paddingHorizontal: spacing.xxxl, paddingBottom: spacing.huge, alignItems: 'center' },
});

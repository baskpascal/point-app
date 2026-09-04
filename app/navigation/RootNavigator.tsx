import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { RootStackParamList } from './types';
import { SplashScreen } from '@/screens/SplashScreen';
import { LiveCameraScreen } from '@/screens/LiveCameraScreen';
import { LiveGuidanceScreen } from '@/screens/LiveGuidanceScreen';
import { StepTimelineScreen } from '@/screens/StepTimelineScreen';
import { DiagnosisScreen } from '@/screens/DiagnosisScreen';
import { PremiumScreen } from '@/screens/PremiumScreen';

const Stack = createNativeStackNavigator<RootStackParamList>();

export function RootNavigator() {
  return (
    <Stack.Navigator
      initialRouteName="Splash"
      screenOptions={{
        headerShown: false,
        animation: 'fade',
        contentStyle: { backgroundColor: '#F7F7F5' },
      }}
    >
      <Stack.Screen name="Splash" component={SplashScreen} />
      <Stack.Screen name="LiveCamera" component={LiveCameraScreen} />
      <Stack.Screen name="LiveGuidance" component={LiveGuidanceScreen} />
      <Stack.Screen name="StepTimeline" component={StepTimelineScreen} />
      <Stack.Screen name="Diagnosis" component={DiagnosisScreen} />
      <Stack.Screen name="Premium" component={PremiumScreen} options={{ presentation: 'modal' }} />
    </Stack.Navigator>
  );
}

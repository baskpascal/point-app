// Point design tokens — soft shadows
// Source of truth: POINT_BUILD_SPEC.md §5.4

import { Platform } from 'react-native';

export const shadows = {
  soft: Platform.select({
    ios: {
      shadowColor: '#101828',
      shadowOffset: { width: 0, height: 8 },
      shadowOpacity: 0.08,
      shadowRadius: 24,
    },
    android: { elevation: 4 },
    default: {},
  }),
  glow: Platform.select({
    ios: {
      shadowColor: '#FF6B6B',
      shadowOffset: { width: 0, height: 6 },
      shadowOpacity: 0.35,
      shadowRadius: 18,
    },
    android: { elevation: 6 },
    default: {},
  }),
};

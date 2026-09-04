// Point design tokens — typography
// Source of truth: POINT_BUILD_SPEC.md §5.3
// Font: SF Pro Display / SF Pro Text (iOS default), Inter fallback (Android/web)

import { Platform } from 'react-native';

const fontFamily = Platform.select({
  ios: 'System', // resolves to SF Pro on iOS
  android: 'sans-serif', // swap for a bundled Inter TTF when available
  default: 'Inter, system-ui, sans-serif',
});

export const typography = {
  fontFamily,
  heroTitle: { fontSize: 46, lineHeight: 52, fontWeight: '700' as const },
  screenTitle: { fontSize: 32, lineHeight: 38, fontWeight: '700' as const },
  bodyLarge: { fontSize: 24, lineHeight: 30, fontWeight: '500' as const },
  body: { fontSize: 17, lineHeight: 24, fontWeight: '400' as const },
  bodyMedium: { fontSize: 17, lineHeight: 24, fontWeight: '600' as const },
  caption: { fontSize: 13, lineHeight: 18, fontWeight: '500' as const },
};

export const weights = {
  regular: '400' as const,
  medium: '500' as const,
  semibold: '600' as const,
  bold: '700' as const,
};

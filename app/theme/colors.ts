// Point design tokens — colors
// Source of truth: POINT_BUILD_SPEC.md §5.1

export const colors = {
  bgPrimary: '#F7F7F5',
  bgSecondary: '#F2F3F5',

  surfaceGlass: 'rgba(255,255,255,0.42)',
  surfaceGlassStrong: 'rgba(255,255,255,0.58)',
  strokeGlass: 'rgba(255,255,255,0.65)',

  textPrimary: '#101828',
  textSecondary: '#667085',
  textTertiary: '#98A2B3',

  accentCoral: '#FF6B6B',
  accentSalmon: '#FF7A73',
  accentOrange: '#FF9E4A',
  accentPeach: '#FFD7C2',

  successSoft: '#78D38B',
  warningSoft: '#F9B35E',

  overlayDark: 'rgba(16,24,40,0.35)',
  white: '#FFFFFF',
  black: '#000000',
} as const;

export type ColorToken = keyof typeof colors;

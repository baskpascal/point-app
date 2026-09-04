// Point design tokens — spacing grid
// Source of truth: POINT_BUILD_SPEC.md §5.5

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  xxl: 24,
  xxxl: 32,
  huge: 40,
} as const;

export type SpacingToken = keyof typeof spacing;

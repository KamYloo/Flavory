import { MD3DarkTheme, MD3LightTheme } from "react-native-paper";

export const colors = {
  primary: "#FF6B35",
  primaryLight: "#FF8A5C",
  primaryDark: "#E65020",

  secondary: "#004E89",
  secondaryLight: "#0066B3",
  secondaryDark: "#003660",

  accent: "#F77F00",
  accentLight: "#FF9933",
  accentDark: "#CC6600",

  background: "#FFFFFF",
  surface: "#F8F9FA",
  surfaceVariant: "#E9ECEF",

  text: "#212529",
  textSecondary: "#6C757D",
  textDisabled: "#ADB5BD",

  error: "#DC3545",
  errorLight: "#F8D7DA",
  success: "#28A745",
  successLight: "#D4EDDA",
  warning: "#FFC107",
  warningLight: "#FFF3CD",
  info: "#17A2B8",
  infoLight: "#D1ECF1",

  border: "#DEE2E6",
  borderLight: "#E9ECEF",

  shadow: "#000000",
  overlay: "rgba(0, 0, 0, 0.5)",
  white: "#FFFFFF",
  black: "#000000",
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
} as const;

export const borderRadius = {
  sm: 4,
  md: 8,
  lg: 12,
  xl: 16,
  round: 9999,
} as const;

export const shadows = {
  sm: {
    shadowColor: colors.shadow,
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  md: {
    shadowColor: colors.shadow,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 4,
    elevation: 4,
  },
  lg: {
    shadowColor: colors.shadow,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 8,
  },
} as const;

export const theme = {
  ...MD3LightTheme,
  colors: {
    ...MD3LightTheme.colors,
    primary: colors.primary,
    secondary: colors.secondary,
    tertiary: colors.accent,
    background: colors.background,
    surface: colors.surface,
    surfaceVariant: colors.surfaceVariant,
    error: colors.error,
    onPrimary: colors.white,
    onSecondary: colors.white,
    onBackground: colors.text,
    onSurface: colors.text,
  },
  roundness: borderRadius.md,
} as const;

export const darkTheme = {
  ...MD3DarkTheme,
  colors: {
    ...MD3DarkTheme.colors,
    primary: colors.primaryLight,
    secondary: colors.secondaryLight,
    tertiary: colors.accentLight,
  },
  roundness: borderRadius.md,
} as const;

export type AppTheme = typeof theme;

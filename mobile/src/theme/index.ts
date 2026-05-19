import { Colors } from './colors';
import { Typography } from './typography';
import { Spacing } from './spacing';

export { Colors, Typography, Spacing };

export const theme = {
  colors: Colors,
  typography: Typography,
  spacing: Spacing,
};

export type Theme = typeof theme;



import { TextStyle } from 'react-native';

export const Typography: Record<string, TextStyle> = {
  hero: {
    fontSize: 22,
    fontWeight: '700',
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
  },
  body: {
    fontSize: 14,
    fontWeight: '400',
  },
  label: {
    fontSize: 11,
    fontWeight: '600',
    letterSpacing: 0.6,
    textTransform: 'uppercase',
  },
  small: {
    fontSize: 12,
    fontWeight: '400',
  },
  button: {
    fontSize: 14,
    fontWeight: '600',
  },
  link: {
    fontSize: 12,
    fontWeight: '500',
  },
} as const;

export type TypographyKey = keyof typeof Typography;



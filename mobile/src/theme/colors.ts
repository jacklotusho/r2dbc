export const Colors = {
  primary: '#4f46e5',
  primaryDark: '#7c3aed',
  primaryGradient: ['#4f46e5', '#7c3aed'],
  background: '#ffffff',
  surface: '#f9fafb',
  border: '#e5e7eb',
  textPrimary: '#111827',
  textSecondary: '#6b7280',
  textMuted: '#9ca3af',
  textOnPrimary: '#ffffff',
  textLink: '#4f46e5',
  error: '#ef4444',
  success: '#10b981',
} as const;

export type ColorKey = keyof typeof Colors;



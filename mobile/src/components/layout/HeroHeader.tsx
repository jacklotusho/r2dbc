import { View, Text, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Colors, Typography, Spacing } from '@/src/theme';

interface HeroHeaderProps {
  logoLetter: string;
  title: string;
  subtitle: string;
  topRightButton?: React.ReactNode;
}

export function HeroHeader({
  logoLetter,
  title,
  subtitle,
  topRightButton,
}: HeroHeaderProps) {
  return (
    <LinearGradient colors={Colors.primaryGradient} style={styles.container}>
      {topRightButton && (
        <View style={styles.topRightButton}>{topRightButton}</View>
      )}

      <View style={styles.logoContainer}>
        <Text style={styles.logoText}>{logoLetter}</Text>
      </View>

      <Text style={styles.title}>{title}</Text>
      <Text style={styles.subtitle}>{subtitle}</Text>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingTop: 36,
    paddingBottom: 32,
    paddingHorizontal: Spacing.xxl,
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
  },
  topRightButton: {
    position: 'absolute',
    top: 50,
    right: Spacing.xl,
  },
  logoContainer: {
    width: 80,
    height: 80,
    borderRadius: 16,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: Spacing.lg,
  },
  logoText: {
    fontSize: 48,
    fontWeight: '700',
    color: Colors.textOnPrimary,
  },
  title: {
    ...Typography.hero,
    color: Colors.textOnPrimary,
    marginBottom: Spacing.sm,
  },
  subtitle: {
    ...Typography.small,
    color: Colors.textOnPrimary,
    opacity: 0.7,
    textAlign: 'center',
  },
});



import { TouchableOpacity, Text, View, StyleSheet } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { Colors, Typography, Spacing } from '@/src/theme';

interface SocialButtonProps {
  provider: 'google' | 'apple' | 'facebook';
  onPress: () => void;
}

const providerConfig = {
  google: {
    icon: 'chrome' as keyof typeof Feather.glyphMap,
    label: 'Continue with Google',
  },
  apple: {
    icon: 'smartphone' as keyof typeof Feather.glyphMap,
    label: 'Continue with Apple',
  },
  facebook: {
    icon: 'facebook' as keyof typeof Feather.glyphMap,
    label: 'Continue with Facebook',
  },
};

export function SocialButton({ provider, onPress }: SocialButtonProps) {
  const config = providerConfig[provider];

  return (
    <TouchableOpacity style={styles.button} onPress={onPress}>
      <View style={styles.iconContainer}>
        <Feather name={config.icon} size={18} color={Colors.textPrimary} />
      </View>
      <Text style={styles.text}>{config.label}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  button: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: Colors.background,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: 10,
    paddingVertical: Spacing.md,
    marginBottom: Spacing.xxl,
    gap: Spacing.sm,
  },
  iconContainer: {
    width: 20,
    height: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  text: {
    ...Typography.small,
    color: Colors.textPrimary,
    fontWeight: '500',
  },
});



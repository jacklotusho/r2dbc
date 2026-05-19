import { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  TextInputProps,
} from 'react-native';
import { Feather } from '@expo/vector-icons';
import { Colors, Typography, Spacing } from '@/src/theme';

interface FormFieldProps extends Omit<TextInputProps, 'style'> {
  label: string;
  leftIcon: keyof typeof Feather.glyphMap;
  rightIcon?: keyof typeof Feather.glyphMap;
  onRightIconPress?: () => void;
  error?: string;
}

export function FormField({
  label,
  leftIcon,
  rightIcon,
  onRightIconPress,
  error,
  ...textInputProps
}: FormFieldProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.label}>{label}</Text>
      <View style={[styles.inputContainer, error && styles.inputContainerError]}>
        <Feather name={leftIcon} size={18} color={Colors.textSecondary} />
        <TextInput
          style={styles.input}
          placeholderTextColor={Colors.textMuted}
          {...textInputProps}
        />
        {rightIcon && (
          <TouchableOpacity onPress={onRightIconPress}>
            <Feather name={rightIcon} size={18} color={Colors.textSecondary} />
          </TouchableOpacity>
        )}
      </View>
      {error && <Text style={styles.errorText}>{error}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 14,
  },
  label: {
    ...Typography.label,
    color: Colors.textSecondary,
    marginBottom: 6,
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.surface,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: 10,
    paddingHorizontal: Spacing.md,
    paddingVertical: 10,
    gap: Spacing.sm,
  },
  inputContainerError: {
    borderColor: Colors.error,
  },
  input: {
    flex: 1,
    ...Typography.body,
    color: Colors.textPrimary,
  },
  errorText: {
    ...Typography.small,
    color: Colors.error,
    marginTop: 4,
  },
});



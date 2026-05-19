import { useState } from 'react';
import { TextInputProps } from 'react-native';
import { FormField } from './FormField';

interface PasswordFieldProps extends Omit<TextInputProps, 'style' | 'secureTextEntry'> {
  label: string;
  error?: string;
}

export function PasswordField({ label, error, ...textInputProps }: PasswordFieldProps) {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <FormField
      label={label}
      leftIcon="lock"
      rightIcon={showPassword ? 'eye-off' : 'eye'}
      onRightIconPress={() => setShowPassword(!showPassword)}
      secureTextEntry={!showPassword}
      error={error}
      {...textInputProps}
    />
  );
}



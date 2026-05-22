import { useState } from 'react';
import { View, Text, TouchableOpacity, Alert, StyleSheet } from 'react-native';
import { Link, useRouter } from 'expo-router';
import { Feather } from '@expo/vector-icons';
import apiClient from '@/lib/api-client';
import { RegisterRequest } from '@/types/auth';
import {
  Screen,
  HeroHeader,
  FormField,
  PasswordField,
  PrimaryButton,
  SocialButton,
  Divider,
} from '@/src/components';
import { Colors, Typography, Spacing } from '@/src/theme';

export default function RegisterScreen() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState<RegisterRequest>({
    username: '',
    email: '',
    password: '',
  });
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState<Partial<RegisterRequest & { confirmPassword: string }>>({});

  const validate = (): boolean => {
    const newErrors: Partial<RegisterRequest & { confirmPassword: string }> = {};
    
    if (!formData.username.trim()) {
      newErrors.username = 'Username is required';
    } else if (formData.username.length < 3) {
      newErrors.username = 'Username must be at least 3 characters';
    }
    
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Invalid email format';
    }
    
    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }
    
    if (!confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
    } else if (formData.password !== confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRegister = async () => {
    if (!validate()) return;

    setIsLoading(true);
    try {
      await apiClient.post('/auth/register', formData);
      Alert.alert(
        'Success',
        'Account created successfully! Please sign in.',
        [{ text: 'OK', onPress: () => router.replace('/(auth)/login') }]
      );
    } catch (error: any) {
      Alert.alert(
        'Registration Failed',
        error.response?.data?.message || 'Unable to create account. Please try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleSignUp = () => {
    Alert.alert('Google Sign Up', 'Google signup not implemented yet');
  };

  return (
    <Screen>
      {/* Hero Header */}
      <HeroHeader
        logoLetter="R"
        title="Create Account"
        subtitle="Join us and get started today"
        topRightButton={
          <TouchableOpacity 
            style={styles.settingsButton}
            onPress={() => router.push('/(auth)/login')}
          >
            <Feather name="arrow-left" size={20} color={Colors.textOnPrimary} />
          </TouchableOpacity>
        }
      />

      {/* Form Section */}
      <View style={styles.formSection}>
        {/* Username Field */}
        <FormField
          label="USERNAME"
          placeholder="Choose a username"
          leftIcon="user"
          value={formData.username}
          onChangeText={(text) => {
            setFormData({ ...formData, username: text });
            setErrors({ ...errors, username: undefined });
          }}
          error={errors.username}
          autoCapitalize="none"
          autoCorrect={false}
        />

        {/* Email Field */}
        <FormField
          label="EMAIL ADDRESS"
          placeholder="Enter your email"
          leftIcon="mail"
          value={formData.email}
          onChangeText={(text) => {
            setFormData({ ...formData, email: text });
            setErrors({ ...errors, email: undefined });
          }}
          error={errors.email}
          keyboardType="email-address"
          autoCapitalize="none"
          autoCorrect={false}
        />

        {/* Password Field */}
        <PasswordField
          label="PASSWORD"
          placeholder="Create a password"
          value={formData.password}
          onChangeText={(text) => {
            setFormData({ ...formData, password: text });
            setErrors({ ...errors, password: undefined });
          }}
          error={errors.password}
        />

        {/* Confirm Password Field */}
        <PasswordField
          label="CONFIRM PASSWORD"
          placeholder="Confirm your password"
          value={confirmPassword}
          onChangeText={(text) => {
            setConfirmPassword(text);
            setErrors({ ...errors, confirmPassword: undefined });
          }}
          error={errors.confirmPassword}
        />

        {/* Sign Up Button */}
        <PrimaryButton
          title="Sign Up"
          onPress={handleRegister}
          isLoading={isLoading}
        />

        {/* Divider */}
        <Divider />

        {/* Google Button */}
        <SocialButton provider="google" onPress={handleGoogleSignUp} />

        {/* Sign In Row */}
        <View style={styles.signInContainer}>
          <Text style={styles.signInText}>Already have an account? </Text>
          <Link href="/(auth)/login" asChild>
            <TouchableOpacity>
              <Text style={styles.signInLink}>Sign in</Text>
            </TouchableOpacity>
          </Link>
        </View>
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  settingsButton: {
    width: 30,
    height: 30,
    borderRadius: 15,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  formSection: {
    flex: 1,
    backgroundColor: Colors.background,
    paddingHorizontal: Spacing.xl,
    paddingTop: Spacing.xl,
  },
  signInContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingBottom: 30,
    marginTop: Spacing.md,
  },
  signInText: {
    fontSize: 13,
    color: Colors.textSecondary,
  },
  signInLink: {
    fontSize: 13,
    color: Colors.textLink,
    fontWeight: '700',
  },
});



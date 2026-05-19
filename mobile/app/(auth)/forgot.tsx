import { useState } from 'react';
import { View, Text, TouchableOpacity, Alert, StyleSheet } from 'react-native';
import { Link, useRouter } from 'expo-router';
import { Feather } from '@expo/vector-icons';
import apiClient from '@/lib/api-client';
import { ForgotPasswordRequest } from '@/types/auth';
import {
  Screen,
  HeroHeader,
  FormField,
  PrimaryButton,
} from '@/src/components';
import { Colors, Typography, Spacing } from '@/src/theme';

export default function ForgotPasswordScreen() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');

  const validate = (): boolean => {
    if (!email.trim()) {
      setError('Email is required');
      return false;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError('Invalid email format');
      return false;
    }
    setError('');
    return true;
  };

  const handleForgotPassword = async () => {
    if (!validate()) return;

    setIsLoading(true);
    try {
      const request: ForgotPasswordRequest = { email };
      await apiClient.post('/auth/forgot-password', request);
      Alert.alert(
        'Success',
        'Password reset instructions have been sent to your email.',
        [{ text: 'OK', onPress: () => router.replace('/(auth)/login') }]
      );
    } catch (error: any) {
      Alert.alert(
        'Error',
        error.response?.data?.message || 'Unable to process request. Please try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Screen>
      {/* Hero Header */}
      <HeroHeader
        logoLetter="🔒"
        title="Forgot Password?"
        subtitle="No worries! Enter your email and we'll send you reset instructions"
      />

      {/* Form Section */}
      <View style={styles.formSection}>
        {/* Email Field */}
        <FormField
          label="EMAIL ADDRESS"
          placeholder="Enter your email"
          leftIcon="mail"
          value={email}
          onChangeText={(text) => {
            setEmail(text);
            setError('');
          }}
          error={error}
          keyboardType="email-address"
          autoCapitalize="none"
          autoCorrect={false}
        />

        {/* Send Reset Link Button */}
        <PrimaryButton
          title="Send Reset Link"
          onPress={handleForgotPassword}
          isLoading={isLoading}
        />

        {/* Back to Sign In Link */}
        <Link href="/(auth)/login" asChild>
          <TouchableOpacity style={styles.backLinkContainer}>
            <Feather name="arrow-left" size={16} color={Colors.textLink} />
            <Text style={styles.backLinkText}>Back to Sign In</Text>
          </TouchableOpacity>
        </Link>

        {/* Sign Up Row */}
        <View style={styles.signUpContainer}>
          <Text style={styles.signUpText}>Don't have an account? </Text>
          <Link href="/(auth)/register" asChild>
            <TouchableOpacity>
              <Text style={styles.signUpLink}>Sign up</Text>
            </TouchableOpacity>
          </Link>
        </View>
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  formSection: {
    flex: 1,
    backgroundColor: Colors.background,
    paddingHorizontal: Spacing.xl,
    paddingTop: Spacing.xl,
  },
  backLinkContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.sm,
    marginTop: Spacing.md,
    marginBottom: Spacing.xxl,
  },
  backLinkText: {
    ...Typography.link,
    color: Colors.textLink,
    fontWeight: '600',
  },
  signUpContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingBottom: 30,
  },
  signUpText: {
    fontSize: 13,
    color: Colors.textSecondary,
  },
  signUpLink: {
    fontSize: 13,
    color: Colors.textLink,
    fontWeight: '700',
  },
});

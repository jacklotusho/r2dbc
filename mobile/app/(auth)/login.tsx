import { useState } from 'react';
import { View, Text, TouchableOpacity, Alert, StyleSheet } from 'react-native';
import { Link, useRouter } from 'expo-router';
import { Feather } from '@expo/vector-icons';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api-client';
import { LoginRequest, AuthResponse } from '@/types/auth';
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

export default function LoginScreen() {
  const router = useRouter();
  const { login } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState<LoginRequest>({
    usernameOrEmail: '',
    password: '',
  });
  const [errors, setErrors] = useState<Partial<LoginRequest>>({});

  const validate = (): boolean => {
    const newErrors: Partial<LoginRequest> = {};
    
    if (!formData.usernameOrEmail.trim()) {
      newErrors.usernameOrEmail = 'Username or email is required';
    }
    
    if (!formData.password) {
      newErrors.password = 'Password is required';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleLogin = async () => {
    if (!validate()) return;

    setIsLoading(true);
    try {
      const response = await apiClient.post<AuthResponse>('/auth/login', formData);
      await login(response.data);
      router.replace('/(tabs)/profile');
    } catch (error: any) {
      Alert.alert(
        'Login Failed',
        error.response?.data?.message || 'Invalid credentials. Please try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    Alert.alert('Google Login', 'Google login not implemented yet');
  };

  return (
    <Screen>
      {/* Hero Header */}
      <HeroHeader
        logoLetter="R"
        title="Welcome back"
        subtitle="Sign in to continue to your account"
        topRightButton={
          <TouchableOpacity style={styles.settingsButton}>
            <Feather name="settings" size={20} color={Colors.textOnPrimary} />
          </TouchableOpacity>
        }
      />

      {/* Form Section */}
      <View style={styles.formSection}>
        {/* Username/Email Field */}
        <FormField
          label="USERNAME OR EMAIL"
          placeholder="Enter your username or email"
          leftIcon="user"
          value={formData.usernameOrEmail}
          onChangeText={(text) => {
            setFormData({ ...formData, usernameOrEmail: text });
            setErrors({ ...errors, usernameOrEmail: undefined });
          }}
          error={errors.usernameOrEmail}
          autoCapitalize="none"
          autoCorrect={false}
        />

        {/* Password Field */}
        <PasswordField
          label="PASSWORD"
          placeholder="Enter your password"
          value={formData.password}
          onChangeText={(text) => {
            setFormData({ ...formData, password: text });
            setErrors({ ...errors, password: undefined });
          }}
          error={errors.password}
        />

        {/* Forgot Password Link */}
        <Link href="/(auth)/forgot" asChild>
          <TouchableOpacity style={styles.forgotPasswordContainer}>
            <Text style={styles.forgotPasswordText}>Forgot password?</Text>
          </TouchableOpacity>
        </Link>

        {/* Sign In Button */}
        <PrimaryButton
          title="Sign in"
          onPress={handleLogin}
          isLoading={isLoading}
        />

        {/* Divider */}
        <Divider />

        {/* Google Button */}
        <SocialButton provider="google" onPress={handleGoogleLogin} />

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
  forgotPasswordContainer: {
    alignSelf: 'flex-end',
    marginTop: -4,
    marginBottom: Spacing.xl,
  },
  forgotPasswordText: {
    ...Typography.link,
    color: Colors.textLink,
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



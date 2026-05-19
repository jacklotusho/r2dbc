import { useState } from 'react';
import { View, Text, ScrollView, KeyboardAvoidingView, Platform, Alert, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Link, useRouter } from 'expo-router';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Card } from '@/components/ui/Card';
import apiClient from '@/lib/api-client';
import { RegisterRequest } from '@/types/auth';

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

  return (
    <LinearGradient
      colors={['#EFF6FF', '#E0E7FF', '#C7D2FE']}
      style={styles.gradient}
    >
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        className="flex-1"
      >
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          showsVerticalScrollIndicator={false}
        >
          <View className="w-full max-w-md mx-auto px-6">
            {/* Header Section */}
            <View className="items-center mb-10 mt-12">
              <View className="w-24 h-24 bg-blue-600 rounded-full items-center justify-center mb-6 shadow-2xl">
                <Text className="text-white text-5xl font-bold">R</Text>
              </View>
              <Text className="text-4xl font-bold text-gray-900 text-center mb-3">
                Create Account
              </Text>
              <Text className="text-gray-600 text-center text-lg px-4">
                Join us and get started today
              </Text>
            </View>

            {/* Form Card */}
            <Card className="mb-6">
          <Input
            label="Username"
            placeholder="Choose a username"
            value={formData.username}
            onChangeText={(text) => {
              setFormData({ ...formData, username: text });
              setErrors({ ...errors, username: undefined });
            }}
            error={errors.username}
            autoCapitalize="none"
            autoCorrect={false}
          />

          <Input
            label="Email"
            placeholder="Enter your email"
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

          <Input
            label="Password"
            placeholder="Create a password"
            value={formData.password}
            onChangeText={(text) => {
              setFormData({ ...formData, password: text });
              setErrors({ ...errors, password: undefined });
            }}
            error={errors.password}
            secureTextEntry
          />

          <Input
            label="Confirm Password"
            placeholder="Confirm your password"
            value={confirmPassword}
            onChangeText={(text) => {
              setConfirmPassword(text);
              setErrors({ ...errors, confirmPassword: undefined });
            }}
            error={errors.confirmPassword}
            secureTextEntry
          />

              <Button
                title="Sign Up"
                onPress={handleRegister}
                isLoading={isLoading}
                className="mt-2"
              />
            </Card>

            {/* Footer */}
            <View className="flex-row justify-center items-center mt-6 mb-8">
              <Text className="text-gray-700 text-base">Already have an account? </Text>
              <Link href="/(auth)/login" asChild>
                <Text className="text-blue-600 font-bold text-base">Sign In</Text>
              </Link>
            </View>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  gradient: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    minHeight: '100%',
  },
});


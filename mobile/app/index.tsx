import { Redirect } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuth } from '@/contexts/AuthContext';
import { View, ActivityIndicator, Text, StyleSheet } from 'react-native';

export default function Index() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <LinearGradient
        colors={['#EFF6FF', '#E0E7FF', '#C7D2FE']}
        style={styles.gradient}
      >
        <View className="flex-1 items-center justify-center">
          <View className="w-28 h-28 bg-blue-600 rounded-full items-center justify-center mb-8 shadow-2xl">
            <Text className="text-white text-6xl font-bold">R</Text>
          </View>
          <ActivityIndicator size="large" color="#3b82f6" />
          <Text className="text-gray-700 mt-6 text-xl font-semibold">Loading...</Text>
        </View>
      </LinearGradient>
    );
  }

  // Redirect based on authentication status
  return <Redirect href={isAuthenticated ? '/(tabs)/profile' : '/(auth)/login'} />;
}

const styles = StyleSheet.create({
  gradient: {
    flex: 1,
  },
});


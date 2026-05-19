import { Stack, Redirect } from 'expo-router';
import { useAuth } from '@/contexts/AuthContext';

export default function AdminLayout() {
  const { user } = useAuth();
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  // Redirect non-admin users to profile
  if (!isAdmin) {
    return <Redirect href="/(tabs)/profile" />;
  }

  return (
    <Stack>
      <Stack.Screen 
        name="users" 
        options={{ 
          title: 'User Management',
          headerShown: false,
        }} 
      />
    </Stack>
  );
}


# R2DBC Auth Mobile - Expo React Native App

A modern Expo React Native app with TypeScript, Expo Router, SecureStore, TanStack Query, and NativeWind for Spring Boot 4 JWT authentication backend.

## Tech Stack

- **Expo SDK 52+** with TypeScript
- **Expo Router** (file-based routing, App Router mental model)
- **expo-secure-store** for encrypted JWT storage
- **TanStack Query v5** for server state management
- **Axios** with interceptor for Bearer token
- **NativeWind** (Tailwind for React Native)
- **React Hook Form** + **Zod** for form validation

## Project Structure

```
mobile/
├── app/
│   ├── (auth)/
│   │   ├── _layout.tsx              # Auth stack layout
│   │   ├── login.tsx                # Login screen
│   │   ├── register.tsx             # Register screen
│   │   └── forgot.tsx               # Forgot password screen
│   ├── (tabs)/
│   │   ├── _layout.tsx              # Tab navigator layout
│   │   ├── profile.tsx              # User profile screen
│   │   └── admin/
│   │       └── users.tsx            # Admin users list (ROLE_ADMIN only)
│   ├── _layout.tsx                  # Root layout with providers
│   └── index.tsx                    # Entry point (redirects based on auth)
├── components/
│   └── ui/
│       ├── Button.tsx               # Custom button component
│       ├── Input.tsx                # Custom input component
│       └── Card.tsx                 # Custom card component
├── contexts/
│   └── AuthContext.tsx              # Auth context provider
├── lib/
│   ├── api-client.ts                # Axios instance with interceptors
│   └── secure-storage.ts            # SecureStore wrapper
├── types/
│   └── auth.ts                      # Auth DTOs (shared with web)
├── app.json                         # Expo configuration
├── package.json                     # Dependencies
├── tsconfig.json                    # TypeScript config
├── tailwind.config.js               # NativeWind config
├── babel.config.js                  # Babel config
└── .env                             # Environment variables
```

## Setup Instructions

### 1. Install Dependencies

```bash
cd mobile
npm install
```

### 2. Install Expo CLI (if not already installed)

```bash
npm install -g expo-cli
```

### 3. Configure Environment Variables

Create `.env`:

```env
EXPO_PUBLIC_API_URL=http://localhost:8080/demo
```

**Note for iOS Simulator/Android Emulator:**
- iOS Simulator: Use `http://localhost:8080/demo`
- Android Emulator: Use `http://10.0.2.2:8080/demo`
- Physical Device: Use your computer's IP address (e.g., `http://192.168.1.100:8080/demo`)

### 4. Run the App

```bash
# Start Expo dev server
npm start

# Run on iOS Simulator
npm run ios

# Run on Android Emulator
npm run android
```

## File Structure Details

### Root Layout (app/_layout.tsx)

```tsx
import { Stack } from 'expo-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '@/contexts/AuthContext';
import { useState } from 'react';
import "../global.css";

export default function RootLayout() {
  const [queryClient] = useState(() => new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 60 * 1000,
        refetchOnWindowFocus: false,
      },
    },
  }));

  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <Stack screenOptions={{ headerShown: false }}>
          <Stack.Screen name="(auth)" />
          <Stack.Screen name="(tabs)" />
        </Stack>
      </AuthProvider>
    </QueryClientProvider>
  );
}
```

### Entry Point (app/index.tsx)

```tsx
import { Redirect } from 'expo-router';
import { useAuth } from '@/contexts/AuthContext';
import { View, ActivityIndicator } from 'react-native';

export default function Index() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <View className="flex-1 items-center justify-center">
        <ActivityIndicator size="large" />
      </View>
    );
  }

  return <Redirect href={isAuthenticated ? '/(tabs)/profile' : '/(auth)/login'} />;
}
```

### Auth Layout (app/(auth)/_layout.tsx)

```tsx
import { Stack } from 'expo-router';

export default function AuthLayout() {
  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Screen name="login" />
      <Stack.Screen name="register" />
      <Stack.Screen name="forgot" />
    </Stack>
  );
}
```

### Login Screen (app/(auth)/login.tsx)

```tsx
import { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, Alert } from 'react-native';
import { useRouter, Link } from 'expo-router';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api-client';
import type { LoginRequest, AuthResponse } from '@/types/auth';

const loginSchema = z.object({
  usernameOrEmail: z.string().min(1, 'Username or email is required'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export default function LoginScreen() {
  const router = useRouter();
  const { login } = useAuth();
  const [isLoading, setIsLoading] = useState(false);

  const { control, handleSubmit, formState: { errors } } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true);
    try {
      const response = await apiClient.post<AuthResponse>('/auth/login', data);
      await login(response.data);
      router.replace('/(tabs)/profile');
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.message || 'Login failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <View className="flex-1 bg-white px-6 justify-center">
      <Text className="text-3xl font-bold mb-8 text-center">Sign In</Text>
      
      <View className="mb-4">
        <Text className="text-sm font-medium mb-2">Username or Email</Text>
        <Controller
          control={control}
          name="usernameOrEmail"
          render={({ field: { onChange, value } }) => (
            <TextInput
              className="border border-gray-300 rounded-lg px-4 py-3"
              placeholder="Enter username or email"
              value={value}
              onChangeText={onChange}
              autoCapitalize="none"
              editable={!isLoading}
            />
          )}
        />
        {errors.usernameOrEmail && (
          <Text className="text-red-500 text-sm mt-1">{errors.usernameOrEmail.message}</Text>
        )}
      </View>

      <View className="mb-4">
        <Text className="text-sm font-medium mb-2">Password</Text>
        <Controller
          control={control}
          name="password"
          render={({ field: { onChange, value } }) => (
            <TextInput
              className="border border-gray-300 rounded-lg px-4 py-3"
              placeholder="Enter password"
              value={value}
              onChangeText={onChange}
              secureTextEntry
              editable={!isLoading}
            />
          )}
        />
        {errors.password && (
          <Text className="text-red-500 text-sm mt-1">{errors.password.message}</Text>
        )}
      </View>

      <Link href="/(auth)/forgot" asChild>
        <TouchableOpacity className="mb-6">
          <Text className="text-blue-600 text-sm">Forgot password?</Text>
        </TouchableOpacity>
      </Link>

      <TouchableOpacity
        className="bg-blue-600 rounded-lg py-4 mb-4"
        onPress={handleSubmit(onSubmit)}
        disabled={isLoading}
      >
        <Text className="text-white text-center font-semibold">
          {isLoading ? 'Signing in...' : 'Sign In'}
        </Text>
      </TouchableOpacity>

      <View className="flex-row justify-center">
        <Text className="text-gray-600">Don't have an account? </Text>
        <Link href="/(auth)/register" asChild>
          <TouchableOpacity>
            <Text className="text-blue-600 font-semibold">Sign up</Text>
          </TouchableOpacity>
        </Link>
      </View>
    </View>
  );
}
```

### Register Screen (app/(auth)/register.tsx)

```tsx
import { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, Alert, ScrollView } from 'react-native';
import { useRouter, Link } from 'expo-router';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import apiClient from '@/lib/api-client';
import type { RegisterRequest } from '@/types/auth';

const registerSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

type RegisterFormData = z.infer<typeof registerSchema>;

export default function RegisterScreen() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);

  const { control, handleSubmit, formState: { errors } } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true);
    try {
      await apiClient.post<RegisterRequest>('/auth/register', data);
      Alert.alert('Success', 'Account created! Please login.', [
        { text: 'OK', onPress: () => router.replace('/(auth)/login') }
      ]);
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.message || 'Registration failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <ScrollView className="flex-1 bg-white">
      <View className="px-6 py-12">
        <Text className="text-3xl font-bold mb-8 text-center">Create Account</Text>
        
        <View className="mb-4">
          <Text className="text-sm font-medium mb-2">Username</Text>
          <Controller
            control={control}
            name="username"
            render={({ field: { onChange, value } }) => (
              <TextInput
                className="border border-gray-300 rounded-lg px-4 py-3"
                placeholder="Choose a username"
                value={value}
                onChangeText={onChange}
                autoCapitalize="none"
                editable={!isLoading}
              />
            )}
          />
          {errors.username && (
            <Text className="text-red-500 text-sm mt-1">{errors.username.message}</Text>
          )}
        </View>

        <View className="mb-4">
          <Text className="text-sm font-medium mb-2">Email</Text>
          <Controller
            control={control}
            name="email"
            render={({ field: { onChange, value } }) => (
              <TextInput
                className="border border-gray-300 rounded-lg px-4 py-3"
                placeholder="you@example.com"
                value={value}
                onChangeText={onChange}
                keyboardType="email-address"
                autoCapitalize="none"
                editable={!isLoading}
              />
            )}
          />
          {errors.email && (
            <Text className="text-red-500 text-sm mt-1">{errors.email.message}</Text>
          )}
        </View>

        <View className="mb-6">
          <Text className="text-sm font-medium mb-2">Password</Text>
          <Controller
            control={control}
            name="password"
            render={({ field: { onChange, value } }) => (
              <TextInput
                className="border border-gray-300 rounded-lg px-4 py-3"
                placeholder="Create a password"
                value={value}
                onChangeText={onChange}
                secureTextEntry
                editable={!isLoading}
              />
            )}
          />
          {errors.password && (
            <Text className="text-red-500 text-sm mt-1">{errors.password.message}</Text>
          )}
        </View>

        <TouchableOpacity
          className="bg-blue-600 rounded-lg py-4 mb-4"
          onPress={handleSubmit(onSubmit)}
          disabled={isLoading}
        >
          <Text className="text-white text-center font-semibold">
            {isLoading ? 'Creating account...' : 'Create Account'}
          </Text>
        </TouchableOpacity>

        <View className="flex-row justify-center">
          <Text className="text-gray-600">Already have an account? </Text>
          <Link href="/(auth)/login" asChild>
            <TouchableOpacity>
              <Text className="text-blue-600 font-semibold">Sign in</Text>
            </TouchableOpacity>
          </Link>
        </View>
      </View>
    </ScrollView>
  );
}
```

### Tabs Layout (app/(tabs)/_layout.tsx)

```tsx
import { Tabs, Redirect } from 'expo-router';
import { useAuth } from '@/contexts/AuthContext';
import { View, ActivityIndicator } from 'react-native';

export default function TabsLayout() {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <View className="flex-1 items-center justify-center">
        <ActivityIndicator size="large" />
      </View>
    );
  }

  if (!isAuthenticated) {
    return <Redirect href="/(auth)/login" />;
  }

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  return (
    <Tabs screenOptions={{ headerShown: true }}>
      <Tabs.Screen
        name="profile"
        options={{
          title: 'Profile',
          tabBarIcon: () => null,
        }}
      />
      {isAdmin && (
        <Tabs.Screen
          name="admin/users"
          options={{
            title: 'Users',
            tabBarIcon: () => null,
          }}
        />
      )}
    </Tabs>
  );
}
```

### Profile Screen (app/(tabs)/profile.tsx)

```tsx
import { View, Text, TouchableOpacity, ScrollView, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api-client';
import type { UserProfileResponse } from '@/types/auth';

export default function ProfileScreen() {
  const router = useRouter();
  const { logout } = useAuth();

  const { data: profile, isLoading, error } = useQuery({
    queryKey: ['user', 'profile'],
    queryFn: async () => {
      const { data } = await apiClient.get<UserProfileResponse>('/api/users/me');
      return data;
    },
  });

  const handleLogout = async () => {
    await logout();
    router.replace('/(auth)/login');
  };

  if (isLoading) {
    return (
      <View className="flex-1 items-center justify-center">
        <ActivityIndicator size="large" />
      </View>
    );
  }

  if (error) {
    return (
      <View className="flex-1 items-center justify-center px-6">
        <Text className="text-red-500 text-center">Failed to load profile</Text>
      </View>
    );
  }

  return (
    <ScrollView className="flex-1 bg-gray-50">
      <View className="p-6">
        <View className="bg-white rounded-lg p-6 mb-4 shadow-sm">
          <Text className="text-2xl font-bold mb-4">Profile</Text>
          
          <View className="mb-4">
            <Text className="text-sm text-gray-500 mb-1">Username</Text>
            <Text className="text-lg font-semibold">{profile?.username}</Text>
          </View>

          <View className="mb-4">
            <Text className="text-sm text-gray-500 mb-1">Email</Text>
            <Text className="text-lg">{profile?.email}</Text>
          </View>

          <View className="mb-4">
            <Text className="text-sm text-gray-500 mb-1">UUID</Text>
            <Text className="text-sm text-gray-600">{profile?.uuid}</Text>
          </View>

          <View className="mb-4">
            <Text className="text-sm text-gray-500 mb-1">Roles</Text>
            <View className="flex-row flex-wrap gap-2 mt-1">
              {profile?.roles.map((role) => (
                <View key={role} className="bg-blue-100 px-3 py-1 rounded-full">
                  <Text className="text-blue-800 text-sm font-medium">{role}</Text>
                </View>
              ))}
            </View>
          </View>

          <View className="mb-4">
            <Text className="text-sm text-gray-500 mb-1">Member Since</Text>
            <Text className="text-sm">{new Date(profile?.createdAt || '').toLocaleDateString()}</Text>
          </View>
        </View>

        <TouchableOpacity
          className="bg-red-600 rounded-lg py-4"
          onPress={handleLogout}
        >
          <Text className="text-white text-center font-semibold">Sign Out</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}
```

### Admin Users Screen (app/(tabs)/admin/users.tsx)

```tsx
import { View, Text, ScrollView, ActivityIndicator } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@/contexts/AuthContext';
import { Redirect } from 'expo-router';
import apiClient from '@/lib/api-client';
import type { UserProfileResponse } from '@/types/auth';

export default function AdminUsersScreen() {
  const { user } = useAuth();
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  const { data: users, isLoading, error } = useQuery({
    queryKey: ['admin', 'users'],
    queryFn: async () => {
      const { data } = await apiClient.get<UserProfileResponse[]>('/api/users');
      return data;
    },
    enabled: isAdmin,
  });

  if (!isAdmin) {
    return <Redirect href="/(tabs)/profile" />;
  }

  if (isLoading) {
    return (
      <View className="flex-1 items-center justify-center">
        <ActivityIndicator size="large" />
      </View>
    );
  }

  if (error) {
    return (
      <View className="flex-1 items-center justify-center px-6">
        <Text className="text-red-500 text-center">Failed to load users</Text>
      </View>
    );
  }

  return (
    <ScrollView className="flex-1 bg-gray-50">
      <View className="p-6">
        <Text className="text-2xl font-bold mb-4">All Users</Text>
        
        {users?.map((user) => (
          <View key={user.uuid} className="bg-white rounded-lg p-4 mb-3 shadow-sm">
            <Text className="text-lg font-semibold mb-2">{user.username}</Text>
            <Text className="text-gray-600 mb-2">{user.email}</Text>
            <View className="flex-row flex-wrap gap-2">
              {user.roles.map((role) => (
                <View key={role} className="bg-blue-100 px-2 py-1 rounded">
                  <Text className="text-blue-800 text-xs">{role}</Text>
                </View>
              ))}
            </View>
          </View>
        ))}
      </View>
    </ScrollView>
  );
}
```

### Global Styles (global.css)

```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

## Authentication Flow

### 1. Login Process
```
User submits login form
→ POST /demo/auth/login
→ Backend returns { token, uuid, username, roles }
→ Save token to SecureStore
→ Save user data to SecureStore
→ Update AuthContext
→ Navigate to /(tabs)/profile
```

### 2. API Requests with Token
```
Axios interceptor reads token from SecureStore
→ Add Authorization: Bearer <token> header
→ Make API request
```

### 3. Token Expiration
```
API returns 401
→ Axios interceptor clears SecureStore
→ AuthContext updates
→ Expo Router redirects to /(auth)/login
```

## Security Notes

1. **JWT Storage**: Tokens stored in SecureStore (encrypted, never AsyncStorage)
2. **Auto-logout**: 401 responses automatically clear auth and redirect
3. **Role Guards**: Admin routes check for ROLE_ADMIN
4. **HTTPS**: Always use HTTPS in production

## Building for Production

### iOS

```bash
expo build:ios
```

### Android

```bash
expo build:android
```

## Troubleshooting

### Cannot connect to backend

**iOS Simulator**: Use `http://localhost:8080/demo`
**Android Emulator**: Use `http://10.0.2.2:8080/demo`
**Physical Device**: Use your computer's IP (e.g., `http://192.168.1.100:8080/demo`)

### CORS Issues

Ensure Spring Boot backend allows mobile origins:

```java
config.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "exp://192.168.1.100:8081"  // Add Expo dev server
));
```

### SecureStore not available

SecureStore requires a physical device or simulator. It won't work in Expo Go on web.

## License

This project is provided as-is for educational purposes.
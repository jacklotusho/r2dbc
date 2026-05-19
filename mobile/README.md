# R2DBC Auth Mobile App

React Native mobile application built with Expo Router, featuring JWT authentication and role-based access control.

## 📱 Features

- **JWT Authentication** with SecureStore (encrypted storage)
- **Role-Based Access Control** (RBAC)
- **Expo Router** file-based navigation
- **TypeScript** for type safety
- **NativeWind** (Tailwind CSS for React Native)
- **React Query** for data fetching
- **Form validation** with React Hook Form & Zod

## 🏗️ Project Structure

```
mobile/
├── app/                          # Expo Router app directory
│   ├── _layout.tsx              # Root layout with providers
│   ├── index.tsx                # Entry point with auth redirect
│   ├── (auth)/                  # Auth group (unauthenticated routes)
│   │   ├── _layout.tsx         # Auth layout with redirect guard
│   │   ├── login.tsx           # Login screen
│   │   ├── register.tsx        # Registration screen
│   │   └── forgot.tsx          # Forgot password screen
│   └── (tabs)/                  # Tab navigation (authenticated routes)
│       ├── _layout.tsx         # Tabs layout with auth guard
│       ├── profile.tsx         # User profile screen
│       └── admin/              # Admin-only routes
│           ├── _layout.tsx     # Admin layout with role guard
│           └── users.tsx       # User management screen
├── components/
│   └── ui/                      # Reusable UI components
│       ├── Button.tsx          # Custom button component
│       ├── Input.tsx           # Custom input component
│       └── Card.tsx            # Custom card component
├── contexts/
│   └── AuthContext.tsx         # Authentication context provider
├── lib/
│   ├── api-client.ts           # Axios instance with interceptors
│   └── secure-storage.ts       # SecureStore wrapper
├── types/
│   └── auth.ts                 # TypeScript DTOs (shared with backend)
├── global.css                   # Tailwind CSS imports
├── nativewind-env.d.ts         # NativeWind type definitions
└── package.json

```

## 🔐 Authentication Flow

### Login Flow
1. User enters credentials in `(auth)/login`
2. POST `/auth/login` → receives JWT token + user data
3. Token saved to **SecureStore** (encrypted)
4. User data saved to SecureStore
5. Navigate to `(tabs)/profile`

### Registration Flow
1. User fills form in `(auth)/register`
2. POST `/auth/register` → creates account
3. Navigate to `(auth)/login`

### Forgot Password Flow
1. User enters email in `(auth)/forgot`
2. POST `/auth/forgot-password` → sends reset email
3. Navigate to `(auth)/login`

### Token Management
- **Axios Interceptor** automatically adds `Authorization: Bearer <token>` header
- On 401 response → clears auth and redirects to login
- Token stored in **SecureStore** (never AsyncStorage - it's unencrypted!)

## 🛡️ Route Guards

### Auth Guard (`(tabs)/_layout.tsx`)
```typescript
if (!isAuthenticated) {
  return <Redirect href="/(auth)/login" />;
}
```

### Role Guard (`(tabs)/admin/_layout.tsx`)
```typescript
const isAdmin = user?.roles?.includes('ROLE_ADMIN');
if (!isAdmin) {
  return <Redirect href="/(tabs)/profile" />;
}
```

### Tab Visibility
```typescript
<Tabs.Screen
  name="admin"
  options={{
    href: isAdmin ? '/admin' : null, // Hide tab if not admin
  }}
/>
```

## 📋 Screens

### Authentication Screens

#### `(auth)/login`
- Username/email + password form
- POST `/auth/login`
- Saves JWT to SecureStore
- Navigates to `(tabs)/profile`

#### `(auth)/register`
- Username, email, password, confirm password
- POST `/auth/register`
- Navigates to login on success

#### `(auth)/forgot`
- Email input
- POST `/auth/forgot-password`
- Sends reset instructions via email

### Authenticated Screens

#### `(tabs)/profile`
- GET `/api/users/me`
- Displays:
  - UUID
  - Username
  - Email
  - Roles (badges)
  - Member since date
- Logout button

#### `(tabs)/admin/users` (ROLE_ADMIN only)
- GET `/api/users`
- Lists all users with:
  - Username & email
  - UUID
  - Roles (color-coded badges)
  - Member since date
- Pull-to-refresh

## 🎨 UI Components

### Button
```tsx
<Button 
  title="Sign In" 
  variant="primary" // primary | secondary | outline
  isLoading={false}
  onPress={handleSubmit}
/>
```

### Input
```tsx
<Input
  label="Email"
  placeholder="Enter email"
  value={email}
  onChangeText={setEmail}
  error={errors.email}
  keyboardType="email-address"
/>
```

### Card
```tsx
<Card className="mb-4">
  <Text>Content here</Text>
</Card>
```

## 🔧 Configuration

### Environment Variables
Create `.env` file:
```env
EXPO_PUBLIC_API_URL=http://localhost:8080/demo
```

### API Base URL
- Development: `http://localhost:8080/demo`
- Production: Update in `.env`

## 📦 Dependencies

```json
{
  "expo": "~52.0.0",
  "expo-router": "~4.0.0",
  "expo-secure-store": "~14.0.0",
  "react-native": "0.76.5",
  "axios": "^1.7.9",
  "@tanstack/react-query": "^5.62.8",
  "nativewind": "^4.1.23",
  "react-hook-form": "^7.54.2",
  "zod": "^3.24.1"
}
```

## 🚀 Getting Started

### Install Dependencies
```bash
cd mobile
npm install
```

### Start Development Server
```bash
npm start
```

### Run on Device/Simulator
```bash
# iOS
npm run ios

# Android
npm run android

# Web (for testing)
npm run web
```

## 🔒 Security Best Practices

### ✅ DO
- Store JWT in **SecureStore** (encrypted)
- Use HTTPS in production
- Validate all user inputs
- Handle 401 responses (token expiry)
- Clear auth on logout

### ❌ DON'T
- Store tokens in AsyncStorage (unencrypted)
- Store sensitive data in plain text
- Trust client-side validation alone
- Hardcode API URLs

## 📱 TypeScript Types

All DTOs match the Spring Boot backend exactly:

```typescript
// Shared types from mobile/types/auth.ts
interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

interface AuthResponse {
  token: string;
  uuid: string;
  username: string;
  roles: string[];
}

interface UserProfileResponse {
  uuid: string;
  username: string;
  email: string;
  roles: string[];
  createdAt: string;
}
```

## 🎯 API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/login` | Login | No |
| POST | `/auth/register` | Register | No |
| POST | `/auth/forgot-password` | Forgot password | No |
| GET | `/api/users/me` | Get current user | Yes |
| GET | `/api/users` | List all users | Yes (ROLE_ADMIN) |

## 🧪 Testing

### Test User Accounts
After running the backend with DataInitializer:

**Regular User:**
- Username: `user`
- Password: `password`
- Roles: `ROLE_USER`

**Admin User:**
- Username: `admin`
- Password: `password`
- Roles: `ROLE_USER`, `ROLE_ADMIN`

## 📝 Notes

- **Expo Router** uses file-based routing
- Groups in parentheses `(auth)` don't appear in URL
- `_layout.tsx` files define nested layouts
- SecureStore is hardware-backed on iOS, encrypted on Android
- NativeWind requires `className` prop (not `style`)

## 🐛 Troubleshooting

### "Cannot connect to server"
- Ensure backend is running on `http://localhost:8080`
- For Android emulator, use `http://10.0.2.2:8080`
- For physical device, use your computer's IP address

### "SecureStore not available"
- SecureStore requires a physical device or simulator
- Not available in Expo Go on web

### TypeScript errors with className
- Ensure `nativewind-env.d.ts` exists
- Restart TypeScript server in VS Code

## 📄 License

MIT
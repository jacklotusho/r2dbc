# Complete Mobile App File Structure

## 📁 Full Directory Tree

```
mobile/
├── app/
│   ├── _layout.tsx                    # Root layout with QueryClient & AuthProvider
│   ├── index.tsx                      # Entry point - redirects based on auth status
│   │
│   ├── (auth)/                        # Authentication routes (unauthenticated)
│   │   ├── _layout.tsx               # Auth layout - redirects if authenticated
│   │   ├── login.tsx                 # Login form → POST /auth/login
│   │   ├── register.tsx              # Register form → POST /auth/register
│   │   └── forgot.tsx                # Forgot password → POST /auth/forgot-password
│   │
│   └── (tabs)/                        # Tab navigation (authenticated)
│       ├── _layout.tsx               # Tabs layout - auth guard + role-based tabs
│       ├── profile.tsx               # Profile screen → GET /api/users/me
│       └── admin/                    # Admin-only routes
│           ├── _layout.tsx           # Admin layout - ROLE_ADMIN guard
│           └── users.tsx             # User list → GET /api/users
│
├── components/
│   └── ui/
│       ├── Button.tsx                # Reusable button (primary/secondary/outline)
│       ├── Input.tsx                 # Reusable input with label & error
│       └── Card.tsx                  # Reusable card container
│
├── contexts/
│   └── AuthContext.tsx               # Auth state management (login/logout/user)
│
├── lib/
│   ├── api-client.ts                 # Axios instance with JWT interceptor
│   └── secure-storage.ts             # SecureStore wrapper for tokens
│
├── types/
│   └── auth.ts                       # TypeScript DTOs (matches backend)
│
├── app.json                          # Expo configuration
├── babel.config.js                   # Babel configuration
├── global.css                        # Tailwind CSS imports
├── nativewind-env.d.ts              # NativeWind type definitions
├── package.json                      # Dependencies
├── tailwind.config.js               # Tailwind configuration
├── tsconfig.json                     # TypeScript configuration
└── README.md                         # Documentation

```

## 📄 File Contents Overview

### Core Configuration Files

#### `app.json`
- Expo app configuration
- App name, slug, version
- Platform-specific settings

#### `package.json`
- Dependencies: expo, expo-router, expo-secure-store, axios, react-query, nativewind
- Scripts: start, ios, android, web

#### `tsconfig.json`
- TypeScript configuration
- Path aliases (@/ → ./)

#### `tailwind.config.js`
- NativeWind configuration
- Content paths for Tailwind

#### `babel.config.js`
- Babel presets for Expo
- NativeWind plugin

### App Structure

#### `app/_layout.tsx` (Root Layout)
```typescript
- QueryClientProvider (React Query)
- AuthProvider (Auth context)
- Stack navigator
  - (auth) group
  - (tabs) group
```

#### `app/index.tsx` (Entry Point)
```typescript
- Check auth status
- Redirect to:
  - /(tabs)/profile if authenticated
  - /(auth)/login if not authenticated
```

### Authentication Routes

#### `app/(auth)/_layout.tsx`
```typescript
- Redirect to tabs if authenticated
- Stack navigator for auth screens
```

#### `app/(auth)/login.tsx`
```typescript
- Login form (username/email + password)
- POST /auth/login
- Save token to SecureStore
- Navigate to profile
```

#### `app/(auth)/register.tsx`
```typescript
- Registration form (username, email, password, confirm)
- Validation (email format, password length, match)
- POST /auth/register
- Navigate to login on success
```

#### `app/(auth)/forgot.tsx`
```typescript
- Email input form
- POST /auth/forgot-password
- Navigate to login after success
```

### Authenticated Routes

#### `app/(tabs)/_layout.tsx`
```typescript
- Auth guard (redirect to login if not authenticated)
- Tab navigator
  - Profile tab (always visible)
  - Admin tab (visible only if ROLE_ADMIN)
```

#### `app/(tabs)/profile.tsx`
```typescript
- GET /api/users/me
- Display user info:
  - UUID
  - Username
  - Email
  - Roles (badges)
  - Member since
- Logout button
```

#### `app/(tabs)/admin/_layout.tsx`
```typescript
- Role guard (ROLE_ADMIN required)
- Redirect to profile if not admin
- Stack navigator for admin screens
```

#### `app/(tabs)/admin/users.tsx`
```typescript
- GET /api/users (admin only)
- List all users with:
  - Username & email
  - UUID
  - Roles (color-coded)
  - Member since
- Pull-to-refresh
```

### UI Components

#### `components/ui/Button.tsx`
```typescript
- Props: title, variant, isLoading, disabled
- Variants: primary (blue), secondary (gray), outline
- Loading state with ActivityIndicator
```

#### `components/ui/Input.tsx`
```typescript
- Props: label, error, ...TextInputProps
- Label above input
- Error message below
- Red border on error
```

#### `components/ui/Card.tsx`
```typescript
- Props: children, className
- White background
- Rounded corners
- Shadow
```

### Context & State

#### `contexts/AuthContext.tsx`
```typescript
- State: user, isLoading, isAuthenticated
- Methods: login(), logout()
- Loads user from SecureStore on mount
- Provides auth state to entire app
```

### API & Storage

#### `lib/api-client.ts`
```typescript
- Axios instance with baseURL
- Request interceptor: adds Bearer token
- Response interceptor: handles 401 (clears auth)
```

#### `lib/secure-storage.ts`
```typescript
- saveToken(token): Store JWT
- getToken(): Retrieve JWT
- saveUser(user): Store user data
- getUser(): Retrieve user data
- clearAuth(): Remove all auth data
```

### Types

#### `types/auth.ts`
```typescript
- LoginRequest
- RegisterRequest
- AuthResponse
- UserProfileResponse
- ForgotPasswordRequest
- ResetPasswordRequest
```

## 🔐 Security Features

### JWT Storage
- **SecureStore** (hardware-backed encryption)
- Never uses AsyncStorage (unencrypted)
- Automatic token injection via Axios interceptor

### Route Guards
1. **Auth Guard**: Redirects unauthenticated users to login
2. **Role Guard**: Redirects non-admin users from admin routes
3. **Tab Visibility**: Hides admin tab for non-admin users

### Token Management
- Automatic Bearer token injection
- 401 response handling (clears auth)
- Token expiry detection

## 🎨 Styling

### NativeWind (Tailwind CSS)
- Uses `className` prop
- Utility-first CSS
- Responsive design
- Color system: blue (primary), gray (neutral), red (admin)

### Component Styling
- Consistent spacing (p-4, mb-4, gap-2)
- Rounded corners (rounded-lg)
- Shadows (shadow-md)
- Color-coded roles (blue for USER, red for ADMIN)

## 🚀 Navigation Flow

```
App Start
    ↓
index.tsx (check auth)
    ↓
    ├─→ Authenticated → (tabs)/profile
    │                      ↓
    │                   Tab Navigator
    │                      ├─→ Profile
    │                      └─→ Admin (if ROLE_ADMIN)
    │                             └─→ Users List
    │
    └─→ Not Authenticated → (auth)/login
                               ↓
                            Auth Stack
                               ├─→ Login
                               ├─→ Register
                               └─→ Forgot Password
```

## 📊 Data Flow

### Login Flow
```
User Input → Validation → POST /auth/login
    ↓
AuthResponse (token, uuid, username, roles)
    ↓
SecureStore.saveToken(token)
SecureStore.saveUser(user)
    ↓
AuthContext.login(authData)
    ↓
Navigate to (tabs)/profile
```

### API Request Flow
```
Component → apiClient.get/post()
    ↓
Request Interceptor
    ↓
SecureStore.getToken()
    ↓
Add Authorization: Bearer <token>
    ↓
Send Request
    ↓
Response Interceptor
    ↓
If 401 → SecureStore.clearAuth() → Redirect to login
If Success → Return data
```

## 🧩 Component Hierarchy

```
App (_layout.tsx)
├── QueryClientProvider
└── AuthProvider
    └── Stack
        ├── (auth) Group
        │   └── Stack
        │       ├── login
        │       ├── register
        │       └── forgot
        │
        └── (tabs) Group
            └── Tabs
                ├── profile
                └── admin
                    └── Stack
                        └── users
```

## 📝 Key Features

✅ File-based routing (Expo Router)
✅ JWT authentication with SecureStore
✅ Role-based access control
✅ Automatic token injection
✅ Auth & role guards
✅ Form validation
✅ Error handling
✅ Loading states
✅ Pull-to-refresh
✅ TypeScript type safety
✅ Shared DTOs with backend
✅ Responsive UI with NativeWind
✅ Reusable components

## 🎯 API Endpoints Used

| Screen | Method | Endpoint | Auth | Role |
|--------|--------|----------|------|------|
| login | POST | /auth/login | No | - |
| register | POST | /auth/register | No | - |
| forgot | POST | /auth/forgot-password | No | - |
| profile | GET | /api/users/me | Yes | - |
| admin/users | GET | /api/users | Yes | ROLE_ADMIN |

## 🔄 State Management

- **AuthContext**: Global auth state (user, isAuthenticated)
- **React Query**: Server state caching (optional, configured but not heavily used)
- **Local State**: Component-level state (forms, loading, errors)

## 📱 Platform Support

- ✅ iOS (Simulator & Device)
- ✅ Android (Emulator & Device)
- ⚠️ Web (Limited - SecureStore not available)

## 🎨 Design System

### Colors
- Primary: Blue (#3b82f6)
- Secondary: Gray (#6b7280)
- Error: Red (#ef4444)
- Success: Green (#10b981)

### Typography
- Headings: Bold, 24-32px
- Body: Regular, 14-16px
- Labels: Medium, 12-14px

### Spacing
- Small: 8px (2)
- Medium: 16px (4)
- Large: 24px (6)

### Components
- Buttons: Rounded (8px), Padding (12px 24px)
- Inputs: Rounded (8px), Border (1px), Padding (12px 16px)
- Cards: Rounded (8px), Shadow, Padding (16px)
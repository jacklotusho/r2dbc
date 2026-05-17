# R2DBC Auth Frontend - Next.js 15 App Router

A modern Next.js 15 frontend with TypeScript, NextAuth.js v5, TanStack Query, and Tailwind CSS for Spring Boot 4 JWT authentication backend.

## Tech Stack

- **Next.js 15** with App Router
- **TypeScript** for type safety
- **NextAuth.js v5** for session management (JWT from backend)
- **TanStack Query v5** for server state management
- **Axios** with request interceptor for Bearer token
- **Tailwind CSS** + **shadcn/ui** components
- **React Hook Form** + **Zod** for form validation

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── api/auth/[...nextauth]/route.ts  # NextAuth API route
│   │   ├── login/page.tsx                    # Login page
│   │   ├── register/page.tsx                 # Register page
│   │   ├── forgot-password/page.tsx          # Forgot password page
│   │   ├── reset-password/page.tsx           # Reset password page
│   │   ├── dashboard/page.tsx                # Protected dashboard
│   │   ├── admin/users/page.tsx              # Admin-only users page
│   │   ├── layout.tsx                        # Root layout
│   │   ├── page.tsx                          # Home (redirects to login)
│   │   ├── providers.tsx                     # Client providers wrapper
│   │   └── globals.css                       # Global styles
│   ├── components/
│   │   └── ui/                               # shadcn/ui components
│   │       ├── button.tsx
│   │       ├── input.tsx
│   │       ├── card.tsx
│   │       ├── label.tsx
│   │       ├── table.tsx
│   │       └── badge.tsx
│   ├── lib/
│   │   ├── api-client.ts                     # Axios instance with interceptors
│   │   ├── auth.ts                           # NextAuth configuration
│   │   └── utils.ts                          # Utility functions
│   └── types/
│       ├── auth.ts                           # Auth-related types (DTOs)
│       └── next-auth.d.ts                    # NextAuth type extensions
├── .env.local                                # Environment variables
├── package.json
├── tsconfig.json
├── tailwind.config.ts
├── postcss.config.mjs
└── next.config.ts
```

## Setup Instructions

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Configure Environment Variables

Create `.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/demo
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=your-secret-key-here-change-in-production
```

Generate a secure NEXTAUTH_SECRET:
```bash
openssl rand -base64 32
```

### 3. Install Additional Dependencies

The package.json includes all necessary dependencies. If you need to add shadcn/ui components:

```bash
# Add missing shadcn/ui dependencies
npm install @radix-ui/react-slot @radix-ui/react-label tailwindcss-animate
```

### 4. Run Development Server

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000)

## Pages Overview

### Public Pages

#### `/login` - Login Page
- Form with username/email and password
- Validates with Zod schema
- Calls NextAuth signIn with credentials provider
- Redirects to `/dashboard` on success

#### `/register` - Registration Page
- Form with username, email, and password
- POST to `/demo/auth/register`
- Auto-login after successful registration
- Redirects to `/dashboard`

#### `/forgot-password` - Forgot Password
- Form with email input
- POST to `/demo/auth/forgot-password`
- Shows success message (security: same message for existing/non-existing emails)

#### `/reset-password` - Reset Password
- Reads `?token=` from URL query params
- Form with newPassword and confirmPassword
- POST to `/demo/auth/reset-password`
- Redirects to `/login` on success

### Protected Pages

#### `/dashboard` - User Dashboard
- Protected route (requires authentication)
- Fetches user profile from `/demo/api/users/me`
- Displays user info: UUID, username, email, roles
- Uses TanStack Query for data fetching

#### `/admin/users` - Admin Users List
- Protected route (requires ROLE_ADMIN)
- Fetches all users from `/demo/api/users`
- Displays users in a table
- Shows 403 error if user doesn't have ROLE_ADMIN

## Authentication Flow

### 1. Login Process
```typescript
// User submits login form
→ NextAuth signIn("credentials", { usernameOrEmail, password })
→ POST /demo/auth/login (via NextAuth authorize function)
→ Backend returns { token, uuid, username, roles }
→ NextAuth stores in JWT session (httpOnly cookie)
→ Redirect to /dashboard
```

### 2. API Requests with Token
```typescript
// Axios interceptor automatically attaches token
→ getSession() retrieves NextAuth session
→ Extract token from session.user.token
→ Add Authorization: Bearer <token> header
→ Make API request
```

### 3. Token Expiration Handling
```typescript
// Axios response interceptor
→ If response status === 401
→ Redirect to /login
→ User must re-authenticate
```

## Type Definitions

### Backend DTOs (src/types/auth.ts)

```typescript
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  uuid: string;
  username: string;
  roles: string[];
}

export interface UserProfileResponse {
  uuid: string;
  username: string;
  email: string;
  roles: string[];
  createdAt: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}
```

## API Client Configuration

### Axios Instance (src/lib/api-client.ts)

```typescript
import axios from 'axios';
import { getSession } from 'next-auth/react';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor - attach Bearer token
apiClient.interceptors.request.use(async (config) => {
  const session = await getSession();
  if (session?.user?.token) {
    config.headers.Authorization = `Bearer ${session.user.token}`;
  }
  return config;
});

// Response interceptor - handle 401
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

## NextAuth Configuration

### src/lib/auth.ts

```typescript
import NextAuth from "next-auth";
import CredentialsProvider from "next-auth/providers/credentials";

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    CredentialsProvider({
      async authorize(credentials) {
        // POST to /demo/auth/login
        const response = await axios.post('/auth/login', {
          usernameOrEmail: credentials.usernameOrEmail,
          password: credentials.password,
        });
        
        if (response.data.token) {
          return {
            id: response.data.uuid,
            token: response.data.token,
            uuid: response.data.uuid,
            username: response.data.username,
            roles: response.data.roles,
          };
        }
        return null;
      },
    }),
  ],
  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        token.token = user.token;
        token.uuid = user.uuid;
        token.username = user.username;
        token.roles = user.roles;
      }
      return token;
    },
    async session({ session, token }) {
      session.user = {
        token: token.token,
        uuid: token.uuid,
        username: token.username,
        roles: token.roles,
      };
      return session;
    },
  },
  session: { strategy: "jwt" },
  pages: { signIn: "/login" },
});
```

## Protected Routes

### Middleware Approach (Optional)

Create `src/middleware.ts` for route protection:

```typescript
import { auth } from "@/lib/auth";
import { NextResponse } from "next/server";

export default auth((req) => {
  const isLoggedIn = !!req.auth;
  const isAdmin = req.auth?.user?.roles?.includes("ROLE_ADMIN");
  
  // Protect /dashboard
  if (req.nextUrl.pathname.startsWith("/dashboard") && !isLoggedIn) {
    return NextResponse.redirect(new URL("/login", req.url));
  }
  
  // Protect /admin routes
  if (req.nextUrl.pathname.startsWith("/admin") && !isAdmin) {
    return NextResponse.redirect(new URL("/403", req.url));
  }
  
  return NextResponse.next();
});

export const config = {
  matcher: ["/dashboard/:path*", "/admin/:path*"],
};
```

## TanStack Query Usage

### Example: Fetch User Profile

```typescript
import { useQuery } from "@tanstack/react-query";
import apiClient from "@/lib/api-client";
import type { UserProfileResponse } from "@/types/auth";

export function useUserProfile() {
  return useQuery({
    queryKey: ["user", "profile"],
    queryFn: async () => {
      const { data } = await apiClient.get<UserProfileResponse>("/api/users/me");
      return data;
    },
  });
}

// In component
const { data: user, isLoading, error } = useUserProfile();
```

## Form Validation with Zod

### Example: Login Schema

```typescript
import * as z from "zod";

const loginSchema = z.object({
  usernameOrEmail: z.string().min(1, "Username or email is required"),
  password: z.string().min(1, "Password is required"),
});

type LoginFormData = z.infer<typeof loginSchema>;

// In component with React Hook Form
const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>({
  resolver: zodResolver(loginSchema),
});
```

## Building for Production

```bash
npm run build
npm start
```

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Backend API base URL | `http://localhost:8080/demo` |
| `NEXTAUTH_URL` | Frontend URL | `http://localhost:3000` |
| `NEXTAUTH_SECRET` | Secret for JWT encryption | Generate with `openssl rand -base64 32` |

## Security Notes

1. **JWT Storage**: Tokens are stored in httpOnly cookies via NextAuth (not localStorage)
2. **CSRF Protection**: NextAuth handles CSRF tokens automatically
3. **Token Refresh**: Implement token refresh logic if backend supports it
4. **HTTPS**: Always use HTTPS in production
5. **Environment Variables**: Never commit `.env.local` to version control

## Troubleshooting

### CORS Issues
If you encounter CORS errors, ensure the Spring Boot backend has CORS configured:

```java
// In SecurityConfig.java
.cors(cors -> cors.configurationSource(request -> {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);
    return config;
}))
```

### TypeScript Errors
Run `npm install` to ensure all dependencies are installed. The TypeScript errors shown during file creation will resolve once dependencies are installed.

### NextAuth Session Issues
- Ensure `NEXTAUTH_SECRET` is set in `.env.local`
- Check that `NEXTAUTH_URL` matches your frontend URL
- Verify backend returns correct JWT token format

## License

This project is provided as-is for educational purposes.
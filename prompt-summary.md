# Project Prompt Summary
> Spring Boot 4 R2DBC + Next.js 15 + Expo — Full Stack Auth App

---

## 1. Backend — Spring Boot 4

### Tech Stack

| Layer | Choice |
|---|---|
| Framework | Spring Boot 4.x · Jakarta EE 11 · Java 21+ |
| Reactive | Spring WebFlux + Spring Data R2DBC |
| Database | PostgreSQL (prod) · H2 R2DBC (tests) |
| Migrations | Flyway (separate JDBC datasource) |
| Auth | Spring Security WebFlux · JWT via jjwt |
| API Docs | springdoc-openapi-starter-webflux-ui v3.0.0 |
| Build | Gradle (Kotlin DSL) or Maven |

---

### Database Schema

```sql
-- V1__init.sql
-- Enable pgcrypto extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create users table
CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create roles table
CREATE TABLE roles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Create user_roles junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_users_uuid ON users(uuid);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- V2__password_reset.sql
CREATE TABLE password_reset_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(64) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT false,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT now(),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);

```

---

### Identity Rules

| Concern | Field | Notes |
|---|---|---|
| DB Primary Key | `id` (Long / BIGSERIAL) | Internal only |
| FK in user_roles | `user_id`, `role_id` (Long) | Internal only |
| JWT `sub` claim | `uuid` (UUID) | `user.uuid.toString()` |
| API responses / DTOs | `uuid` (UUID) | Never expose `id` |
| Security principal | `uuid` (UUID object) | Cast from SecurityContext |
| Never exposed externally | `id` (Long) | — |

---

### Entities

```java
// User.java
@Table("users")
public class User {
    @Id Long id;
    UUID uuid;
    String username;
    String email;
    String passwordHash;
    boolean enabled;
    LocalDateTime createdAt;
}

// Role.java
@Table("roles")
public class Role {
    @Id Long id;
    String name;
}

// UserRole.java
@Table("user_roles")
public class UserRole {
    Long userId;
    Long roleId;
}

// PasswordResetToken.java
@Table("password_reset_tokens")
public class PasswordResetToken {
    @Id Long id;
    Long userId;
    String token;
    LocalDateTime expiresAt;
    boolean used;
    LocalDateTime usedAt;
    LocalDateTime createdAt;
}
```

---

### Repositories

```java
UserRepository extends R2dbcRepository<User, Long>
  Mono<User> findByUsername(String username)
  Mono<User> findByEmail(String email)
  Mono<User> findByUuid(UUID uuid)

RoleRepository extends R2dbcRepository<Role, Long>
  Mono<Role> findByName(String name)
  Flux<Role> findByIdIn(List<Long> ids)

UserRoleRepository extends R2dbcRepository<UserRole, Long>
  Flux<UserRole> findByUserId(Long userId)

PasswordResetTokenRepository extends R2dbcRepository<PasswordResetToken, Long>
  Mono<PasswordResetToken> findByToken(String token)
  Mono<Void> deleteByUserId(Long userId)
```

---

### Services

**UserService**
- `registerUser(RegisterRequest)` → hash password, save user, assign ROLE_USER
- `findByUuid(UUID)` → `Mono<User>`
- `findByUsername(String)` → `Mono<User>`
- `getUserRoles(Long userId)` → `findByUserId` → extract roleIds → `findByIdIn`

**AuthService**
- `login(LoginRequest)` → verify password, load roles, issue JWT with `sub = user.uuid.toString()`
- `resolveUserByUuid(UUID)` → `Mono<UserDetails>` (used by JWT filter)
- Login lookup: `findByUsername` first, then `findByEmail` via `Mono.switchIfEmpty`

**PasswordResetService**
- `forgotPassword(String email)` → find user silently → generate SecureRandom token → save with 1hr expiry → send email
- `resetPassword(ResetPasswordRequest)` → validate token (not used, not expired) → BCrypt encode new password → save user → mark token used=true, usedAt=now

**EmailService**
- `sendPasswordResetEmail(String toEmail, String resetLink)` → `Mono<Void>`
- Uses `Mono.fromCallable(() -> { send SimpleMailMessage; return null; }).subscribeOn(Schedulers.boundedElastic())`

---

### JWT Utility

```java
// JwtUtil.java
String generateToken(UUID userUuid, List<String> roles)
  // sub = userUuid.toString(), roles claim, iat, exp

boolean validateToken(String token)
UUID extractUserUuid(String token)   // parse sub as UUID
List<String> extractRoles(String token)
```

---

### Security Config

```java
@EnableWebFluxSecurity
SecurityWebFilterChain:
  - Stateless (no session/cookies)
  - JWT WebFilter: extract Bearer → validate → extractUserUuid
                   → load User by UUID → set Authentication (principal = UUID)
  - Public:        POST /auth/register, /auth/login
                   POST /auth/forgot-password, /auth/reset-password
                   GET  /v3/api-docs/**, /swagger-ui/**, /webjars/**
  - Authenticated: GET /api/users/me
  - ROLE_ADMIN:    GET /api/users, POST /api/admin/**
```

---

### Controllers & DTOs

**AuthController**
- `POST /auth/register` → `RegisterRequest` → `Mono<AuthResponse>`
- `POST /auth/login` → `LoginRequest` → `Mono<AuthResponse>`

**UserController**
- `GET /api/users/me` → extract UUID from SecurityContext → `UserProfileResponse`
- `GET /api/users` → `Flux<UserProfileResponse>` (ROLE_ADMIN only)

**PasswordResetController**
- `POST /auth/forgot-password` → `ForgotPasswordRequest` → always `200 OK`
- `POST /auth/reset-password` → `ResetPasswordRequest` → `200 OK`

**DTOs**

```
RegisterRequest       { username, email, password }
LoginRequest          { usernameOrEmail, password }   ← try username then email
AuthResponse          { token, uuid, username, roles }
UserProfileResponse   { uuid, username, email, roles, createdAt }
ForgotPasswordRequest { email }
ResetPasswordRequest  { token, newPassword, confirmPassword }
```

> `id` (Long) is never in any DTO or response. `password_hash` is never in any response.

---

### Swagger / OpenAPI

```yaml
# application.yml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method
```

```java
// OpenApiConfig.java — JWT Authorize button
@Bean
public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info().title("User Auth API").version("1.0.0"))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
        .components(new Components().addSecuritySchemes("Bearer Auth",
            new SecurityScheme()
                .type(HTTP).scheme("bearer").bearerFormat("JWT")));
}
```

Disable in production: `springdoc.swagger-ui.enabled=false` in `application-prod.yml`

---

### application.yml

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/yourdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  flyway:
    url: jdbc:postgresql://localhost:5432/yourdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

app:
  frontend-base-url: ${FRONTEND_BASE_URL:http://localhost:3000}
  reset-token-expiry-minutes: 60

jwt:
  secret: ${JWT_SECRET}
  expiration-ms: 86400000
```

---

### Backend Hard Constraints

- All imports must be `jakarta.*` not `javax.*` (Spring Boot 4 / Jakarta EE 11)
- Never call `.block()` anywhere — fully reactive chain throughout
- JWT `sub` = `user.uuid.toString()` only — never username, email, or Long id
- Internal Long `id` never exposed in any DTO or API response
- `password_hash` must never appear in any response DTO
- Flyway needs a separate JDBC `DataSource` bean — R2DBC URL is not compatible
- Use `@EnableWebFluxSecurity`, NOT `@EnableWebSecurity`
- R2DBC has no ORM joins — all joins handled manually
- `LoginRequest` tries `findByUsername` first, then `findByEmail` via `switchIfEmpty`
- Forgot password always returns `200` — never reveal if email exists
- Reset token: `SecureRandom`, Base64 URL-safe, 32 bytes, single-use, 1hr expiry, never logged
- `@EnableR2dbcRepositories` must be explicitly declared
- `ReactiveSecurityContextHolder` principal must be cast to `UUID` in controllers

---

## 2. Web Frontend — Next.js 15

### Tech Stack

| Layer | Choice |
|---|---|
| Framework | Next.js 15 · App Router · TypeScript |
| Auth | NextAuth.js v5 · JWT in httpOnly cookie |
| Server state | TanStack Query v5 |
| HTTP | Axios + Bearer interceptor from NextAuth session |
| Styling | Tailwind CSS + shadcn/ui |
| Forms | React Hook Form + Zod |

---

### Pages

| Route | Description |
|---|---|
| `/login` | LoginForm → POST /auth/login → store session → redirect /dashboard |
| `/register` | RegisterForm → POST /auth/register → redirect /login |
| `/forgot-password` | ForgotPasswordForm → POST /auth/forgot-password → success message |
| `/reset-password` | Reads `?token=` from URL → ResetPasswordForm → POST /auth/reset-password |
| `/dashboard` | Protected · GET /api/users/me profile card |
| `/admin/users` | Protected + ROLE_ADMIN · user list table |

---

### Auth Rules

- Unauthenticated users → redirect to `/login` from all protected pages
- `ROLE_USER` accessing `/admin/*` → redirect to 403 page
- JWT stored in httpOnly cookie via NextAuth — **never localStorage**
- Axios interceptor reads token from NextAuth session → attaches as `Authorization: Bearer` header

---

### Web Hard Constraints

- JWT in httpOnly cookie via NextAuth — never localStorage
- DTO field names must match backend exactly (`usernameOrEmail`, not `username`)
- Shared TypeScript types in `packages/types` (monorepo)
- Shared Zod schemas in `packages/validation`

---

## 3. Mobile Frontend — Expo

### Tech Stack

| Layer | Choice |
|---|---|
| Framework | Expo SDK 52+ · React Native · TypeScript |
| Routing | Expo Router (file-based, like Next.js App Router) |
| Token storage | expo-secure-store (encrypted) |
| Server state | TanStack Query v5 |
| HTTP | Axios + interceptor reads JWT from SecureStore |
| Styling | NativeWind (Tailwind syntax for React Native) |
| Forms | React Hook Form + Zod |
| Icons | @expo/vector-icons (Feather set) |
| Gradients | expo-linear-gradient |

---

### Screens

| Route | Description |
|---|---|
| `(auth)/login` | LoginForm → POST /auth/login → save to SecureStore → navigate (tabs) |
| `(auth)/register` | RegisterForm → POST /auth/register → navigate login |
| `(auth)/forgot` | ForgotPasswordForm → POST /auth/forgot-password |
| `(tabs)/profile` | GET /api/users/me → uuid, username, email, roles |
| `(tabs)/admin/users` | ROLE_ADMIN only · GET /api/users list |

---

### Auth Rules

- Unauthenticated → Expo Router redirect to `(auth)/login`
- Non-admin accessing admin tab → redirect to profile
- Deep link `/reset-password?token=` handled for password reset flow

---

### Mobile Hard Constraints

- JWT stored in `expo-secure-store` only — **never AsyncStorage** (unencrypted)
- Use `KeyboardAvoidingView` to prevent keyboard from covering inputs
- Gradients via `expo-linear-gradient`, not CSS
- Icons via `@expo/vector-icons` Feather set
- Wrap root screen in `SafeAreaView` from `react-native-safe-area-context`
- Never use `localStorage`/`sessionStorage` — use React state or SecureStore

---

## 4. Design System (Expo)

### Theme Tokens

```typescript
// src/theme/colors.ts
export const Colors = {
  primary:          '#4f46e5',
  primaryDark:      '#7c3aed',
  primaryGradient:  ['#4f46e5', '#7c3aed'],
  background:       '#ffffff',
  surface:          '#f9fafb',
  border:           '#e5e7eb',
  textPrimary:      '#111827',
  textSecondary:    '#6b7280',
  textMuted:        '#9ca3af',
  textOnPrimary:    '#ffffff',
  textLink:         '#4f46e5',
  error:            '#ef4444',
  success:          '#10b981',
};

// src/theme/typography.ts
export const Typography = {
  hero:   { fontSize: 22, fontWeight: '700' },
  title:  { fontSize: 18, fontWeight: '600' },
  body:   { fontSize: 14, fontWeight: '400' },
  label:  { fontSize: 11, fontWeight: '600', letterSpacing: 0.6, textTransform: 'uppercase' },
  small:  { fontSize: 12, fontWeight: '400' },
  button: { fontSize: 14, fontWeight: '600' },
  link:   { fontSize: 12, fontWeight: '500' },
};

// src/theme/spacing.ts  (4-point grid)
export const Spacing = {
  xs: 4, sm: 8, md: 12, lg: 16, xl: 20, xxl: 24, xxxl: 32,
};
```

---

### Shared Components

| Component | Description |
|---|---|
| `Screen` | SafeAreaView + KeyboardAvoidingView + optional ScrollView |
| `HeroHeader` | Gradient header · logoLetter · title · subtitle · optional topRightButton |
| `FormField` | label + left icon + TextInput + optional right icon + error text |
| `PasswordField` | FormField with eye/eye-off toggle built in |
| `PrimaryButton` | Gradient bg + Animated.spring press scale + loading spinner |
| `SocialButton` | White bordered · icon + label (Google, Apple, etc.) |
| `LinkText` | Tappable text · align prop (left / center / right) |
| `Divider` | "or continue with" between two horizontal lines |

---

### File Structure

```
src/
  theme/
    colors.ts
    typography.ts
    spacing.ts
    index.ts          ← re-exports as `theme`
  components/
    layout/
      Screen.tsx
      HeroHeader.tsx
    form/
      FormField.tsx
      PasswordField.tsx
    ui/
      PrimaryButton.tsx
      SocialButton.tsx
      LinkText.tsx
      Divider.tsx
    index.ts          ← re-exports all components
```

---

### Screen Usage Pattern

```tsx
// Any auth screen
<Screen>
  <HeroHeader
    logoLetter="R"
    title="Welcome back"
    subtitle="Sign in to continue"
    topRightButton={<SettingsButton />}
  />
  <View style={styles.form}>
    <FormField label="Username or email" leftIcon="user" ... />
    <PasswordField value={password} onChangeText={setPassword} />
    <LinkText align="right" onPress={goToForgot}>Forgot password?</LinkText>
    <PrimaryButton label="Sign in" onPress={handleLogin} loading={isLoading} />
    <Divider />
    <SocialButton label="Continue with Google" icon={<GoogleIcon />} onPress={...} />
  </View>
</Screen>
```

---

## 5. Monorepo Structure

```
apps/
  web/          ← Next.js 15 (App Router)
  mobile/       ← Expo (React Native)
packages/
  api/          ← shared Axios client + API calls
  types/        ← shared TypeScript types (match Java DTOs exactly)
  validation/   ← shared Zod schemas
```

Use **Turborepo** to manage the monorepo.

---

## 6. Shared TypeScript Types

```typescript
// packages/types/index.ts

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  usernameOrEmail: string;   // ← must match backend field name exactly
  password: string;
}

export interface AuthResponse {
  token: string;
  uuid: string;              // ← UUID, never Long id
  username: string;
  roles: string[];
}

export interface UserProfileResponse {
  uuid: string;              // ← UUID, never Long id
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

---

## 7. Forgot Password Flow

```
[User] POST /auth/forgot-password { email }
           ↓
     Find user by email
     (silently ignore if not found — always return 200)
           ↓
     Generate SecureRandom token (32 bytes, Base64 URL-safe)
           ↓
     Save to password_reset_tokens (expires_at = now + 1hr)
           ↓
     Send email → {frontend-url}/reset-password?token=xxx
           ↓
     Return 200 OK (generic message always)

[User] POST /auth/reset-password { token, newPassword, confirmPassword }
           ↓
     Find token → validate not used, not expired
           ↓
     Find user by token.userId
           ↓
     BCrypt encode newPassword → update users.password_hash
           ↓
     Mark token used=true and set used_at=now()
           ↓
     Return 200 OK
```

---

## 8. Common Debugging Checklist

| # | Check | Command |
|---|---|---|
| 1 | Admin user exists in DB | `SELECT * FROM users WHERE username = 'admin'` |
| 2 | Password hash is BCrypt | Hash must start with `$2a$` |
| 3 | Hash matches password | `encoder.matches("plain", "$2a$hash")` must return `true` |
| 4 | Correct JSON payload | Log payload before sending: `console.log(JSON.stringify(body))` |
| 5 | Isolate frontend vs backend | Test with curl directly against `/auth/login` |
| 6 | User is enabled | `SELECT enabled FROM users WHERE username = 'admin'` |
| 7 | Field name matches | Backend expects `usernameOrEmail`, not `username` |

```bash
# Curl test — bypasses the app entirely
curl -X POST http://YOUR_IP:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"yourPassword"}'
```

---

*Generated from conversation — Spring Boot 4 + R2DBC + Next.js 15 + Expo auth project*

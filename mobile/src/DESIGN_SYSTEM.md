# Design System Documentation

This document describes the design system structure and usage for the Expo React Native app.

## File Structure

```
src/
  theme/
    colors.ts        - Color tokens
    typography.ts    - Font styles
    spacing.ts       - Spacing scale
    index.ts         - Theme exports
  components/
    layout/
      Screen.tsx         - Base screen wrapper
      HeroHeader.tsx     - Gradient header component
    form/
      FormField.tsx      - Input field with label and icons
      PasswordField.tsx  - Password field with visibility toggle
    ui/
      PrimaryButton.tsx  - Gradient button with animation
      SocialButton.tsx   - Social login button
      LinkText.tsx       - Tappable text link
      Divider.tsx        - Text divider
    index.ts             - Component exports
```

## Theme

### Colors (`src/theme/colors.ts`)

```typescript
import { Colors } from '@/src/theme';

// Available colors:
Colors.primary          // #4f46e5 (indigo)
Colors.primaryDark      // #7c3aed (violet)
Colors.primaryGradient  // ['#4f46e5', '#7c3aed']
Colors.background       // #ffffff
Colors.surface          // #f9fafb
Colors.border           // #e5e7eb
Colors.textPrimary      // #111827
Colors.textSecondary    // #6b7280
Colors.textMuted        // #9ca3af
Colors.textOnPrimary    // #ffffff
Colors.textLink         // #4f46e5
Colors.error            // #ef4444
Colors.success          // #10b981
```

### Typography (`src/theme/typography.ts`)

```typescript
import { Typography } from '@/src/theme';

// Available styles:
Typography.hero    // 22px, bold
Typography.title   // 18px, semibold
Typography.body    // 14px, regular
Typography.label   // 11px, semibold, uppercase, letter-spacing
Typography.small   // 12px, regular
Typography.button  // 14px, semibold
Typography.link    // 12px, medium
```

### Spacing (`src/theme/spacing.ts`)

4-point grid system:

```typescript
import { Spacing } from '@/src/theme';

Spacing.xs    // 4
Spacing.sm    // 8
Spacing.md    // 12
Spacing.lg    // 16
Spacing.xl    // 20
Spacing.xxl   // 24
Spacing.xxxl  // 32
```

## Components

### Layout Components

#### Screen

Base screen wrapper with SafeAreaView, KeyboardAvoidingView, and optional ScrollView.

```tsx
import { Screen } from '@/src/components';

// Scrollable (default)
<Screen>
  {/* content */}
</Screen>

// Non-scrollable
<Screen scrollable={false}>
  {/* content */}
</Screen>

// With custom style
<Screen style={{ padding: 20 }}>
  {/* content */}
</Screen>
```

#### HeroHeader

Gradient header with logo, title, subtitle, and optional top-right button.

```tsx
import { HeroHeader } from '@/src/components';

<HeroHeader
  logoLetter="R"
  title="Welcome back"
  subtitle="Sign in to continue to your account"
  topRightButton={
    <TouchableOpacity>
      <Feather name="settings" size={20} color="#fff" />
    </TouchableOpacity>
  }
/>
```

### Form Components

#### FormField

Input field with label, left icon, and optional right icon.

```tsx
import { FormField } from '@/src/components';

<FormField
  label="USERNAME OR EMAIL"
  placeholder="Enter your username or email"
  leftIcon="user"
  value={value}
  onChangeText={setValue}
  error={error}
  autoCapitalize="none"
/>

// With right icon
<FormField
  label="SEARCH"
  placeholder="Search..."
  leftIcon="search"
  rightIcon="x"
  onRightIconPress={clearSearch}
  value={searchQuery}
  onChangeText={setSearchQuery}
/>
```

#### PasswordField

Password field with built-in visibility toggle.

```tsx
import { PasswordField } from '@/src/components';

<PasswordField
  label="PASSWORD"
  placeholder="Enter your password"
  value={password}
  onChangeText={setPassword}
  error={error}
/>
```

### UI Components

#### PrimaryButton

Gradient button with press animation and loading state.

```tsx
import { PrimaryButton } from '@/src/components';

<PrimaryButton
  title="Sign in"
  onPress={handleLogin}
  isLoading={isLoading}
/>

<PrimaryButton
  title="Submit"
  onPress={handleSubmit}
  disabled={!isValid}
/>
```

#### SocialButton

Social login button with provider icon.

```tsx
import { SocialButton } from '@/src/components';

<SocialButton provider="google" onPress={handleGoogleLogin} />
<SocialButton provider="apple" onPress={handleAppleLogin} />
<SocialButton provider="facebook" onPress={handleFacebookLogin} />
```

#### LinkText

Tappable text link in primary color.

```tsx
import { LinkText } from '@/src/components';

<LinkText onPress={handlePress}>
  Forgot password?
</LinkText>

<LinkText onPress={handlePress} style={{ fontSize: 14 }}>
  Learn more
</LinkText>
```

#### Divider

Horizontal divider with centered text.

```tsx
import { Divider } from '@/src/components';

<Divider />  // Default: "or continue with"
<Divider text="or" />
<Divider text="More options" />
```

## Usage Example

Complete login screen using the design system:

```tsx
import { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Feather } from '@expo/vector-icons';
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
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  return (
    <Screen>
      <HeroHeader
        logoLetter="R"
        title="Welcome back"
        subtitle="Sign in to continue"
        topRightButton={
          <TouchableOpacity style={styles.settingsButton}>
            <Feather name="settings" size={20} color="#fff" />
          </TouchableOpacity>
        }
      />

      <View style={styles.formSection}>
        <FormField
          label="EMAIL"
          placeholder="Enter your email"
          leftIcon="mail"
          value={email}
          onChangeText={setEmail}
          keyboardType="email-address"
        />

        <PasswordField
          label="PASSWORD"
          placeholder="Enter your password"
          value={password}
          onChangeText={setPassword}
        />

        <PrimaryButton
          title="Sign in"
          onPress={handleLogin}
          isLoading={isLoading}
        />

        <Divider />

        <SocialButton provider="google" onPress={handleGoogleLogin} />
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
});
```

## Best Practices

1. **Always use theme tokens** instead of hardcoded values:
   ```tsx
   // ✅ Good
   color: Colors.textPrimary
   fontSize: Typography.body.fontSize
   padding: Spacing.xl
   
   // ❌ Bad
   color: '#111827'
   fontSize: 14
   padding: 20
   ```

2. **Use design system components** instead of building from scratch:
   ```tsx
   // ✅ Good
   <FormField label="EMAIL" leftIcon="mail" ... />
   
   // ❌ Bad
   <View>
     <Text>EMAIL</Text>
     <TextInput ... />
   </View>
   ```

3. **Extend components when needed**:
   ```tsx
   // Create specialized components that use design system
   export function SearchField(props) {
     return (
       <FormField
         leftIcon="search"
         rightIcon="x"
         placeholder="Search..."
         {...props}
       />
     );
   }
   ```

4. **Maintain consistency** across all screens by using the same components and patterns.

## Adding New Components

When adding new components to the design system:

1. Place them in the appropriate directory (`layout/`, `form/`, or `ui/`)
2. Use theme tokens for all styling
3. Export from `src/components/index.ts`
4. Document usage in this file
5. Ensure TypeScript types are properly defined

## Migration Guide

To migrate existing screens to use the design system:

1. Replace hardcoded colors with `Colors.*`
2. Replace hardcoded typography with `Typography.*`
3. Replace hardcoded spacing with `Spacing.*`
4. Replace custom input fields with `FormField` or `PasswordField`
5. Replace custom buttons with `PrimaryButton` or `SocialButton`
6. Wrap screens with `Screen` component
7. Use `HeroHeader` for auth screens
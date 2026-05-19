# Troubleshooting Guide

## Issue: App shows "Hello World" instead of login screen

This is likely a Metro bundler cache issue. Follow these steps:

### Solution 1: Clear Metro Cache and Restart

1. **Stop the current Expo server** (press `Ctrl+C` in the terminal where it's running)

2. **Clear all caches:**
```bash
cd mobile
rm -rf node_modules
rm -rf .expo
rm package-lock.json
npm install
```

3. **Start with cache cleared:**
```bash
npm start -- --clear
```

4. **In the Expo app, shake device and select "Reload"** or press `r` in the terminal

### Solution 2: Reset Metro Bundler

If the app is still running:

1. In the terminal where Expo is running, press:
   - `r` - Reload app
   - `shift+r` - Reload and clear cache

2. Or in the Expo Go app:
   - Shake device
   - Select "Reload"

### Solution 3: Complete Reset

```bash
cd mobile

# Clear all caches
rm -rf node_modules
rm -rf .expo
rm -rf .expo-shared
rm package-lock.json

# Clear Metro bundler cache
npx expo start --clear

# Or use watchman (if installed)
watchman watch-del-all

# Reinstall
npm install

# Start fresh
npm start
```

### Solution 4: Check File Structure

Ensure these files exist:
```
mobile/
├── app/
│   ├── _layout.tsx          ✓ Root layout
│   ├── index.tsx            ✓ Entry point (redirects to login)
│   ├── (auth)/
│   │   ├── _layout.tsx     ✓ Auth layout
│   │   ├── login.tsx       ✓ Login screen
│   │   ├── register.tsx    ✓ Register screen
│   │   └── forgot.tsx      ✓ Forgot password
│   └── (tabs)/
│       ├── _layout.tsx     ✓ Tabs layout
│       ├── profile.tsx     ✓ Profile screen
│       └── admin/
│           ├── _layout.tsx ✓ Admin layout
│           └── users.tsx   ✓ Users screen
```

### Solution 5: Verify Expo Router Setup

Check `package.json` has correct entry point:
```json
{
  "main": "expo-router/entry"
}
```

### Solution 6: iOS Simulator Reset

If using iOS Simulator:
```bash
# Reset simulator
xcrun simctl erase all

# Or from Simulator menu:
# Device > Erase All Content and Settings
```

### Solution 7: Android Emulator Reset

If using Android Emulator:
```bash
# Clear app data
adb shell pm clear host.exp.exponent

# Or from Settings:
# Apps > Expo Go > Storage > Clear Data
```

### Common Issues

#### Port Already in Use
```
Error: Port 8081 is already in use
```

**Solution:**
```bash
# Find and kill the process
lsof -ti:8081 | xargs kill -9

# Or use a different port
npm start -- --port 8082
```

#### Module Not Found
```
Error: Unable to resolve module @/...
```

**Solution:**
```bash
# Reinstall dependencies
rm -rf node_modules
npm install

# Clear cache
npm start -- --clear
```

#### NativeWind Not Working
```
Error: className prop not recognized
```

**Solution:**
1. Ensure `nativewind-env.d.ts` exists
2. Restart TypeScript server in VS Code
3. Check `tailwind.config.js` content paths

#### SecureStore Not Available
```
Error: SecureStore is not available
```

**Solution:**
- SecureStore requires a physical device or simulator
- Not available in Expo Go on web
- Use iOS Simulator or Android Emulator for testing

### Verification Steps

After clearing cache, verify the app loads correctly:

1. **App should redirect to login screen** (not "Hello World")
2. **Login screen should show:**
   - "Welcome Back" title
   - Username/Email input
   - Password input
   - Sign In button
   - Forgot Password link
   - Sign Up link

3. **Test navigation:**
   - Click "Sign Up" → should go to register screen
   - Click "Forgot Password" → should go to forgot password screen
   - Click back → should return to login

### Still Not Working?

If the app still shows "Hello World":

1. **Check if `app/index.tsx` was properly updated:**
```typescript
// Should contain redirect logic, NOT "Hello World"
import { Redirect } from 'expo-router';
import { useAuth } from '@/contexts/AuthContext';
```

2. **Verify `app/_layout.tsx` exists and wraps with AuthProvider**

3. **Check Metro bundler logs** for any errors

4. **Try deleting the app** from device/simulator and reinstalling

### Debug Mode

Enable debug logging:
```bash
# Start with verbose logging
EXPO_DEBUG=true npm start
```

### Contact Support

If none of these solutions work:
1. Check Expo logs in terminal
2. Check device/simulator logs
3. Verify all files were created correctly
4. Ensure you're using compatible versions (Expo 52, React Native 0.76.5)
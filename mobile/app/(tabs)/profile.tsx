import { useEffect, useState } from 'react';
import { View, Text, Alert, ActivityIndicator, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api-client';
import { UserProfileResponse } from '@/types/auth';
import { Screen, PrimaryButton } from '@/src/components';
import { Colors, Typography, Spacing } from '@/src/theme';

export default function ProfileScreen() {
  const router = useRouter();
  const { logout, user: authUser } = useAuth();
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await apiClient.get<UserProfileResponse>('/api/users/me');
      setProfile(response.data);
    } catch (error: any) {
      Alert.alert(
        'Error',
        error.response?.data?.message || 'Failed to load profile'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    Alert.alert(
      'Logout',
      'Are you sure you want to logout?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Logout',
          style: 'destructive',
          onPress: async () => {
            await logout();
            router.replace('/(auth)/login');
          },
        },
      ]
    );
  };

  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  return (
    <Screen scrollable={false}>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}
      >
        {/* Header with Avatar */}
        <LinearGradient colors={Colors.primaryGradient} style={styles.headerSection}>
          {/* Logout Button in Header */}
          <TouchableOpacity
            style={styles.headerLogoutButton}
            onPress={handleLogout}
          >
            <Text style={styles.headerLogoutText}>Logout</Text>
          </TouchableOpacity>
          
          <View style={styles.avatarContainer}>
          <Text style={styles.avatarText}>
            {(profile?.username || authUser?.username || 'U').charAt(0).toUpperCase()}
          </Text>
        </View>
        <Text style={styles.username}>
          {profile?.username || authUser?.username}
        </Text>
        <Text style={styles.email}>
          {profile?.email || 'No email provided'}
        </Text>
      </LinearGradient>

      {/* Profile Info Section */}
      <View style={styles.contentSection}>
        {/* User ID Card */}
        <View style={styles.infoCard}>
          <Text style={styles.infoLabel}>USER ID</Text>
          <Text style={styles.infoValue}>
            {profile?.uuid || authUser?.uuid}
          </Text>
        </View>

        {/* Roles Card */}
        <View style={styles.infoCard}>
          <Text style={styles.infoLabel}>ROLES</Text>
          <View style={styles.rolesContainer}>
            {(profile?.roles || authUser?.roles || []).map((role) => (
              <View key={role} style={styles.roleBadge}>
                <LinearGradient
                  colors={Colors.primaryGradient}
                  style={styles.roleBadgeGradient}
                >
                  <Text style={styles.roleText}>
                    {role.replace('ROLE_', '')}
                  </Text>
                </LinearGradient>
              </View>
            ))}
          </View>
        </View>

        {/* Member Since Card */}
        {profile?.createdAt && (
          <View style={styles.infoCard}>
            <Text style={styles.infoLabel}>MEMBER SINCE</Text>
            <Text style={styles.infoValueLarge}>
              {new Date(profile.createdAt).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </Text>
          </View>
        )}

        {/* Logout Button */}
        <View style={styles.logoutButtonContainer}>
          <TouchableOpacity
            style={styles.logoutButton}
            onPress={handleLogout}
          >
            <Text style={styles.logoutButtonText}>Logout</Text>
          </TouchableOpacity>
        </View>
      </View>
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    backgroundColor: Colors.background,
    justifyContent: 'center',
    alignItems: 'center',
  },
  headerSection: {
    paddingTop: 60,
    paddingBottom: 40,
    paddingHorizontal: Spacing.xxl,
    alignItems: 'center',
    position: 'relative',
  },
  headerLogoutButton: {
    position: 'absolute',
    top: 60,
    right: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.5)',
  },
  headerLogoutText: {
    color: Colors.textOnPrimary,
    fontSize: 14,
    fontWeight: '600',
  },
  avatarContainer: {
    width: 100,
    height: 100,
    borderRadius: 50,
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: Spacing.lg,
    borderWidth: 4,
    borderColor: 'rgba(255, 255, 255, 0.5)',
  },
  avatarText: {
    fontSize: 48,
    fontWeight: '700',
    color: Colors.textOnPrimary,
  },
  username: {
    ...Typography.hero,
    fontSize: 28,
    color: Colors.textOnPrimary,
    marginBottom: Spacing.xs,
  },
  email: {
    ...Typography.body,
    color: 'rgba(255, 255, 255, 0.8)',
  },
  contentSection: {
    backgroundColor: Colors.background,
    paddingHorizontal: Spacing.xl,
    paddingTop: Spacing.xxl,
    paddingBottom: 100,
  },
  infoCard: {
    backgroundColor: Colors.surface,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: 12,
    padding: Spacing.lg,
    marginBottom: Spacing.lg,
  },
  infoLabel: {
    ...Typography.label,
    color: Colors.textSecondary,
    marginBottom: Spacing.sm,
  },
  infoValue: {
    ...Typography.body,
    color: Colors.textPrimary,
    fontFamily: 'monospace',
    backgroundColor: Colors.background,
    padding: Spacing.md,
    borderRadius: 8,
  },
  infoValueLarge: {
    ...Typography.body,
    fontSize: 16,
    fontWeight: '600',
    color: Colors.textPrimary,
  },
  rolesContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.sm,
  },
  roleBadge: {
    borderRadius: 20,
    overflow: 'hidden',
  },
  roleBadgeGradient: {
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.sm,
  },
  roleText: {
    ...Typography.small,
    fontWeight: '700',
    color: Colors.textOnPrimary,
  },
  logoutButtonContainer: {
    marginTop: Spacing.xxl,
    marginBottom: 40,
    alignItems: 'center',
  },
  logoutButton: {
    backgroundColor: '#FF3B30',
    paddingHorizontal: 40,
    paddingVertical: 16,
    borderRadius: 12,
    width: '100%',
    alignItems: 'center',
  },
  logoutButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
  },
  scrollContent: {
    flexGrow: 1,
  },
});



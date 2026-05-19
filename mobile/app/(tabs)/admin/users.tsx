import { useEffect, useState } from 'react';
import { View, Text, ScrollView, Alert, ActivityIndicator, RefreshControl, StyleSheet, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { Card } from '@/components/ui/Card';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api-client';
import { UserProfileResponse } from '@/types/auth';
import { Colors, Typography, Spacing } from '@/src/theme';

export default function AdminUsersScreen() {
  const router = useRouter();
  const { logout } = useAuth();
  const [users, setUsers] = useState<UserProfileResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async (isRefresh = false) => {
    if (isRefresh) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    try {
      const response = await apiClient.get<UserProfileResponse[]>('/api/users');
      setUsers(response.data);
    } catch (error: any) {
      Alert.alert(
        'Error',
        error.response?.data?.message || 'Failed to load users'
      );
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const onRefresh = () => {
    fetchUsers(true);
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
    <View style={styles.container}>
      {/* Header with Gradient */}
      <LinearGradient colors={Colors.primaryGradient} style={styles.headerSection}>
        {/* Logout Button in Header */}
        <TouchableOpacity
          style={styles.headerLogoutButton}
          onPress={handleLogout}
        >
          <Text style={styles.headerLogoutText}>Logout</Text>
        </TouchableOpacity>
        
        <Text style={styles.headerTitle}>User Management</Text>
        <Text style={styles.headerSubtitle}>Total Users: {users.length}</Text>
      </LinearGradient>

      {/* Users List */}
      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={styles.scrollContent}
        refreshControl={
          <RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} />
        }
      >

        {users.length === 0 ? (
          <View style={styles.infoCard}>
            <Text style={styles.emptyText}>No users found</Text>
          </View>
        ) : (
          users.map((user) => (
            <View key={user.uuid} style={styles.infoCard}>
              <View style={styles.userHeader}>
                <View style={styles.avatarSmall}>
                  <Text style={styles.avatarSmallText}>
                    {user.username.charAt(0).toUpperCase()}
                  </Text>
                </View>
                <View style={styles.userInfo}>
                  <Text style={styles.username}>{user.username}</Text>
                  <Text style={styles.email}>{user.email}</Text>
                </View>
              </View>

              <View style={styles.divider} />

              <View style={styles.section}>
                <Text style={styles.infoLabel}>UUID</Text>
                <Text style={styles.infoValue}>{user.uuid}</Text>
              </View>

              <View style={styles.section}>
                <Text style={styles.infoLabel}>ROLES</Text>
                <View style={styles.rolesContainer}>
                  {user.roles.map((role) => (
                    <View key={role} style={styles.roleBadge}>
                      <LinearGradient
                        colors={role === 'ROLE_ADMIN' ? ['#EF4444', '#DC2626'] : Colors.primaryGradient}
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

              {user.createdAt && (
                <View style={styles.section}>
                  <Text style={styles.infoLabel}>MEMBER SINCE</Text>
                  <Text style={styles.memberSince}>
                    {new Date(user.createdAt).toLocaleDateString('en-US', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                    })}
                  </Text>
                </View>
              )}
            </View>
          ))
        )}

        {/* Bottom Logout Button */}
        <View style={styles.logoutButtonContainer}>
          <TouchableOpacity
            style={styles.logoutButton}
            onPress={handleLogout}
          >
            <Text style={styles.logoutButtonText}>Logout</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  loadingContainer: {
    flex: 1,
    backgroundColor: Colors.background,
    justifyContent: 'center',
    alignItems: 'center',
  },
  headerSection: {
    paddingTop: 60,
    paddingBottom: 30,
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
  headerTitle: {
    ...Typography.hero,
    fontSize: 28,
    color: Colors.textOnPrimary,
    marginBottom: Spacing.xs,
  },
  headerSubtitle: {
    ...Typography.body,
    color: 'rgba(255, 255, 255, 0.8)',
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: Spacing.xl,
    paddingTop: Spacing.xl,
    paddingBottom: 40,
  },
  infoCard: {
    backgroundColor: Colors.surface,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: 12,
    padding: Spacing.lg,
    marginBottom: Spacing.lg,
  },
  userHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: Spacing.md,
  },
  avatarSmall: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: Colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: Spacing.md,
  },
  avatarSmallText: {
    fontSize: 20,
    fontWeight: '700',
    color: Colors.textOnPrimary,
  },
  userInfo: {
    flex: 1,
  },
  username: {
    ...Typography.body,
    fontSize: 18,
    fontWeight: '700',
    color: Colors.textPrimary,
    marginBottom: 4,
  },
  email: {
    ...Typography.small,
    color: Colors.textSecondary,
  },
  divider: {
    height: 1,
    backgroundColor: Colors.border,
    marginVertical: Spacing.md,
  },
  section: {
    marginBottom: Spacing.md,
  },
  infoLabel: {
    ...Typography.label,
    color: Colors.textSecondary,
    marginBottom: Spacing.xs,
  },
  infoValue: {
    ...Typography.small,
    color: Colors.textPrimary,
    fontFamily: 'monospace',
    backgroundColor: Colors.background,
    padding: Spacing.sm,
    borderRadius: 6,
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
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.xs,
  },
  roleText: {
    ...Typography.small,
    fontSize: 12,
    fontWeight: '700',
    color: Colors.textOnPrimary,
  },
  memberSince: {
    ...Typography.body,
    fontSize: 14,
    fontWeight: '600',
    color: Colors.textPrimary,
  },
  emptyText: {
    ...Typography.body,
    color: Colors.textSecondary,
    textAlign: 'center',
  },
  logoutButtonContainer: {
    marginTop: Spacing.xl,
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
});


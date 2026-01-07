import React from "react";
import { Alert, ScrollView, StyleSheet, View } from "react-native";
import {
  ActivityIndicator,
  Avatar,
  Button,
  Card,
  Chip,
  Divider,
  IconButton,
  Text,
} from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";
import { useAuth, useCurrentUser } from "../hooks/useAuth";
import { colors, shadows, spacing } from "../../../theme";
import { UserStatus } from "../../../types";

export default function ProfileScreen() {
  const { logout } = useAuth();
  const { data: user, isLoading, error, refetch } = useCurrentUser();

  const handleLogout = () => {
    Alert.alert(
      "Logout",
      "Are you sure you want to logout?",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Logout",
          style: "destructive",
          onPress: async () => {
            try {
              await logout();
            } catch (err) {
              Alert.alert("Error", "Failed to logout. Please try again.");
            }
          },
        },
      ],
      { cancelable: true },
    );
  };

  if (isLoading) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text variant="bodyMedium" style={styles.loadingText}>
          Loading profile...
        </Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.centerContainer}>
        <Text variant="titleMedium" style={styles.errorText}>
          Failed to load profile
        </Text>
        <Button
          mode="outlined"
          onPress={() => refetch()}
          style={styles.retryButton}
        >
          Retry
        </Button>
      </View>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <SafeAreaView style={styles.container} edges={["bottom"]}>
      <ScrollView showsVerticalScrollIndicator={false}>
        {/* Header with Avatar */}
        <View style={styles.header}>
          <View style={styles.avatarContainer}>
            {user.profileImageUrl ? (
              <Avatar.Image size={100} source={{ uri: user.profileImageUrl }} />
            ) : (
              <Avatar.Text
                size={100}
                label={user.firstName?.[0] || "U"}
                style={styles.avatarText}
              />
            )}
            <IconButton
              icon="camera"
              size={20}
              mode="contained"
              containerColor={colors.primary}
              iconColor={colors.white}
              style={styles.cameraButton}
              onPress={() => Alert.alert("Coming Soon", "Photo upload feature")}
            />
          </View>

          <Text variant="headlineSmall" style={styles.name}>
            {user.fullName}
          </Text>
          <Text variant="bodyMedium" style={styles.email}>
            {user.email}
          </Text>

          {/* Status Chips */}
          <View style={styles.chipsContainer}>
            <Chip
              icon={user.isVerified ? "check-circle" : "alert-circle"}
              mode="outlined"
              style={[
                styles.chip,
                user.isVerified ? styles.verifiedChip : styles.unverifiedChip,
              ]}
            >
              {user.isVerified ? "Verified" : "Not Verified"}
            </Chip>
            <Chip icon="account" mode="outlined" style={styles.chip}>
              {user.role}
            </Chip>
          </View>
        </View>

        <Card style={styles.card}>
          <Card.Content>
            <Text variant="titleMedium" style={styles.sectionTitle}>
              Account Information
            </Text>
            <Divider style={styles.divider} />

            <InfoRow label="First Name" value={user.firstName} />
            <InfoRow label="Last Name" value={user.lastName} />
            <InfoRow
              label="Phone Number"
              value={user.phoneNumber || "Not provided"}
            />
            <InfoRow
              label="Account Status"
              value={getStatusLabel(user.status)}
            />
            <InfoRow
              label="Member Since"
              value={new Date(user.createdAt).toLocaleDateString()}
            />
          </Card.Content>
        </Card>

        <Card style={styles.card}>
          <Card.Content>
            <Text variant="titleMedium" style={styles.sectionTitle}>
              Settings
            </Text>
            <Divider style={styles.divider} />

            <Button
              mode="outlined"
              icon="pencil"
              onPress={() => Alert.alert("Coming Soon", "Edit profile feature")}
              style={styles.actionButton}
            >
              Edit Profile
            </Button>

            <Button
              mode="outlined"
              icon="map-marker"
              onPress={() =>
                Alert.alert("Coming Soon", "Manage addresses feature")
              }
              style={styles.actionButton}
            >
              Manage Addresses
            </Button>

            <Button
              mode="outlined"
              icon="bell"
              onPress={() =>
                Alert.alert("Coming Soon", "Notifications settings")
              }
              style={styles.actionButton}
            >
              Notifications
            </Button>
          </Card.Content>
        </Card>

        <Button
          mode="outlined"
          icon="logout"
          onPress={handleLogout}
          style={styles.logoutButton}
          textColor={colors.error}
        >
          Logout
        </Button>

        <View style={styles.bottomSpacer} />
      </ScrollView>
    </SafeAreaView>
  );
}

const InfoRow = ({ label, value }: { label: string; value: string }) => (
  <View style={styles.infoRow}>
    <Text variant="bodyMedium" style={styles.infoLabel}>
      {label}
    </Text>
    <Text variant="bodyMedium" style={styles.infoValue}>
      {value}
    </Text>
  </View>
);

const getStatusLabel = (status: UserStatus): string => {
  const labels: Record<UserStatus, string> = {
    ACTIVE: "Active",
    INACTIVE: "Inactive",
    SUSPENDED: "Suspended",
    PENDING_VERIFICATION: "Pending Verification",
  };
  return labels[status] || status;
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  centerContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    padding: spacing.lg,
  },
  header: {
    alignItems: "center",
    padding: spacing.lg,
    backgroundColor: colors.surface,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  avatarContainer: {
    position: "relative",
    marginBottom: spacing.md,
  },
  avatarText: {
    backgroundColor: colors.primary,
  },
  cameraButton: {
    position: "absolute",
    right: 0,
    bottom: 0,
  },
  name: {
    fontWeight: "bold",
    marginTop: spacing.sm,
  },
  email: {
    color: colors.textSecondary,
    marginTop: spacing.xs,
  },
  chipsContainer: {
    flexDirection: "row",
    gap: spacing.sm,
    marginTop: spacing.md,
  },
  chip: {
    borderColor: colors.border,
  },
  verifiedChip: {
    borderColor: colors.success,
  },
  unverifiedChip: {
    borderColor: colors.warning,
  },
  card: {
    margin: spacing.md,
    ...shadows.sm,
  },
  sectionTitle: {
    fontWeight: "600",
    marginBottom: spacing.xs,
  },
  divider: {
    marginVertical: spacing.md,
  },
  infoRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingVertical: spacing.sm,
  },
  infoLabel: {
    color: colors.textSecondary,
    flex: 1,
  },
  infoValue: {
    fontWeight: "600",
    flex: 1,
    textAlign: "right",
  },
  actionButton: {
    marginBottom: spacing.sm,
  },
  logoutButton: {
    margin: spacing.md,
    marginTop: spacing.sm,
    borderColor: colors.error,
  },
  loadingText: {
    marginTop: spacing.md,
    color: colors.textSecondary,
  },
  errorText: {
    color: colors.error,
    marginBottom: spacing.md,
  },
  retryButton: {
    marginTop: spacing.sm,
  },
  bottomSpacer: {
    height: spacing.lg,
  },
});

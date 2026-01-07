import React, { useState } from "react";
import { Alert, StyleSheet, View } from "react-native";
import { ActivityIndicator, Button, Text } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";
import { useAuth } from "../hooks/useAuth";
import { colors, spacing } from "../../../theme";

export default function LoginScreen() {
  const { login, error } = useAuth();
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = async () => {
    setIsLoading(true);

    try {
      await login();
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "Failed to login";
      Alert.alert("Login Error", errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container} edges={["top", "bottom"]}>
      <View style={styles.content}>
        <View style={styles.logoContainer}>
          <View style={styles.logoPlaceholder}>
            <Text variant="displayLarge" style={styles.logoText}>
              🍽️
            </Text>
          </View>
          <Text variant="displaySmall" style={styles.title}>
            Flavory
          </Text>
          <Text variant="bodyLarge" style={styles.subtitle}>
            Discover delicious home-cooked meals near you
          </Text>
        </View>

        {error && (
          <View style={styles.errorContainer}>
            <Text variant="bodyMedium" style={styles.errorText}>
              {error}
            </Text>
          </View>
        )}

        <View style={styles.buttonContainer}>
          {isLoading ? (
            <ActivityIndicator size="large" color={colors.primary} />
          ) : (
            <Button
              mode="contained"
              onPress={handleLogin}
              style={styles.loginButton}
              contentStyle={styles.loginButtonContent}
              labelStyle={styles.loginButtonLabel}
              disabled={isLoading}
            >
              Sign in with Auth0
            </Button>
          )}

          <Text variant="bodySmall" style={styles.disclaimer}>
            By continuing, you agree to our Terms of Service and Privacy Policy
          </Text>
        </View>
      </View>

      <View style={styles.footer}>
        <Text variant="bodySmall" style={styles.footerText}>
          Made with ❤️ for food lovers
        </Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    flex: 1,
    justifyContent: "center",
    padding: spacing.lg,
  },
  logoContainer: {
    alignItems: "center",
    marginBottom: spacing.xxl,
  },
  logoPlaceholder: {
    width: 120,
    height: 120,
    borderRadius: 60,
    backgroundColor: colors.primaryLight,
    justifyContent: "center",
    alignItems: "center",
    marginBottom: spacing.md,
  },
  logoText: {
    fontSize: 64,
  },
  title: {
    fontWeight: "bold",
    color: colors.primary,
    marginBottom: spacing.sm,
  },
  subtitle: {
    color: colors.textSecondary,
    textAlign: "center",
    paddingHorizontal: spacing.lg,
  },
  errorContainer: {
    backgroundColor: colors.errorLight,
    padding: spacing.md,
    borderRadius: 8,
    marginBottom: spacing.lg,
  },
  errorText: {
    color: colors.error,
    textAlign: "center",
  },
  buttonContainer: {
    marginTop: spacing.xl,
  },
  loginButton: {
    borderRadius: 8,
  },
  loginButtonContent: {
    paddingVertical: spacing.sm,
  },
  loginButtonLabel: {
    fontSize: 16,
    fontWeight: "600",
  },
  disclaimer: {
    color: colors.textSecondary,
    textAlign: "center",
    marginTop: spacing.md,
    paddingHorizontal: spacing.md,
  },
  footer: {
    padding: spacing.md,
    alignItems: "center",
  },
  footerText: {
    color: colors.textSecondary,
  },
});

import React from "react";
import { StyleSheet, View } from "react-native";
import { Text } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";
import { colors, spacing } from "../theme";

interface PlaceholderScreenProps {
  title?: string;
  message?: string;
}

export default function PlaceholderScreen({
  title = "Coming Soon",
  message = "This feature is currently under development.",
}: PlaceholderScreenProps) {
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.icon}>🚧</Text>
        <Text variant="headlineMedium" style={styles.title}>
          {title}
        </Text>
        <Text variant="bodyLarge" style={styles.message}>
          {message}
        </Text>
        <Text variant="bodyMedium" style={styles.info}>
          We're working hard to bring you this feature soon!
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
    alignItems: "center",
    padding: spacing.xl,
  },
  icon: {
    fontSize: 80,
    marginBottom: spacing.lg,
  },
  title: {
    fontWeight: "bold",
    textAlign: "center",
    marginBottom: spacing.md,
  },
  message: {
    textAlign: "center",
    color: colors.textSecondary,
    marginBottom: spacing.sm,
  },
  info: {
    textAlign: "center",
    color: colors.textSecondary,
    fontStyle: "italic",
  },
});

import React, { useEffect } from "react";
import { StyleSheet } from "react-native";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { ActivityIndicator, Text } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";
import { useAuth, useCurrentUser } from "../features/auth/hooks/useAuth";
import { useAuthStore } from "../features/auth/store/authStore";
import LoginScreen from "../features/auth/screens/LoginScreen";
import TabNavigator from "./TabNavigator";
import { colors, spacing } from "../theme";

export type RootStackParamList = {
  Login: undefined;
  Main: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

const LoadingScreen = () => (
  <SafeAreaView style={styles.loadingContainer}>
    <ActivityIndicator size="large" color={colors.primary} />
    <Text variant="bodyMedium" style={styles.loadingText}>
      Loading...
    </Text>
  </SafeAreaView>
);

export default function AppNavigator() {
  const { isAuthenticated, credentials } = useAuth();
  const { data: user, isLoading: isLoadingUser } = useCurrentUser();
  const { setUser } = useAuthStore();

  useEffect(() => {
    if (user) {
      setUser(user);
    }
  }, [user, setUser]);

  if (isAuthenticated && !user && isLoadingUser) {
    return <LoadingScreen />;
  }

  return (
    <NavigationContainer>
      <Stack.Navigator
        screenOptions={{
          headerShown: false,
          animation: "fade",
        }}
      >
        {!isAuthenticated || !credentials ? (
          <Stack.Screen
            name="Login"
            component={LoginScreen}
            options={{
              animationTypeForReplace: !isAuthenticated ? "pop" : "push",
            }}
          />
        ) : (
          <Stack.Screen name="Main" component={TabNavigator} />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: colors.background,
  },
  loadingText: {
    marginTop: spacing.md,
    color: colors.textSecondary,
  },
});

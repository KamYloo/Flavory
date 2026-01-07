import React from "react";
import { Pressable, ScrollView, StyleSheet, View } from "react-native";
import { Card, Chip, Searchbar, Text } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";
import { useCurrentUser } from "../features/auth/hooks/useAuth";
import { borderRadius, colors, shadows, spacing } from "../theme";

const CATEGORIES = [
  { id: 1, name: "Italian", icon: "🍝" },
  { id: 2, name: "Asian", icon: "🍜" },
  { id: 3, name: "Mexican", icon: "🌮" },
  { id: 4, name: "American", icon: "🍔" },
  { id: 5, name: "Desserts", icon: "🍰" },
];

const FEATURED_DISHES = [
  {
    id: 1,
    name: "Homemade Lasagna",
    cook: "Maria Rossi",
    rating: 4.8,
    price: 45.99,
    image: "🍝",
  },
  {
    id: 2,
    name: "Thai Green Curry",
    cook: "Chef Tom",
    rating: 4.9,
    price: 38.5,
    image: "🍛",
  },
  {
    id: 3,
    name: "Beef Tacos",
    cook: "Carlos M.",
    rating: 4.7,
    price: 32.0,
    image: "🌮",
  },
];

export default function HomeScreen() {
  const { data: user } = useCurrentUser();
  const [searchQuery, setSearchQuery] = React.useState("");

  return (
    <SafeAreaView style={styles.container} edges={["bottom"]}>
      <ScrollView showsVerticalScrollIndicator={false}>
        <View style={styles.header}>
          <View>
            <Text variant="bodyLarge" style={styles.greeting}>
              Hello, {user?.firstName || "Guest"} 👋
            </Text>
            <Text variant="headlineMedium" style={styles.title}>
              What would you like to eat?
            </Text>
          </View>
        </View>

        <View style={styles.searchContainer}>
          <Searchbar
            placeholder="Search for dishes..."
            onChangeText={setSearchQuery}
            value={searchQuery}
            style={styles.searchBar}
            elevation={0}
          />
        </View>

        <View style={styles.section}>
          <Text variant="titleMedium" style={styles.sectionTitle}>
            Categories
          </Text>
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.categoriesContainer}
          >
            {CATEGORIES.map((category) => (
              <Pressable
                key={category.id}
                style={styles.categoryCard}
                onPress={() => console.log("Category:", category.name)}
              >
                <Text style={styles.categoryIcon}>{category.icon}</Text>
                <Text variant="bodyMedium" style={styles.categoryName}>
                  {category.name}
                </Text>
              </Pressable>
            ))}
          </ScrollView>
        </View>

        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text variant="titleMedium" style={styles.sectionTitle}>
              Featured Dishes
            </Text>
            <Text variant="bodySmall" style={styles.seeAll}>
              See all
            </Text>
          </View>

          {FEATURED_DISHES.map((dish) => (
            <Card
              key={dish.id}
              style={styles.dishCard}
              onPress={() => console.log("Dish:", dish.name)}
            >
              <View style={styles.dishContent}>
                <View style={styles.dishImage}>
                  <Text style={styles.dishEmoji}>{dish.image}</Text>
                </View>

                <View style={styles.dishInfo}>
                  <Text variant="titleMedium" style={styles.dishName}>
                    {dish.name}
                  </Text>
                  <Text variant="bodySmall" style={styles.cookName}>
                    by {dish.cook}
                  </Text>

                  <View style={styles.dishFooter}>
                    <View style={styles.rating}>
                      <Text style={styles.ratingIcon}>⭐</Text>
                      <Text variant="bodySmall" style={styles.ratingText}>
                        {dish.rating}
                      </Text>
                    </View>
                    <Text variant="titleMedium" style={styles.price}>
                      {dish.price.toFixed(2)} PLN
                    </Text>
                  </View>
                </View>
              </View>
            </Card>
          ))}
        </View>

        <Card style={styles.banner}>
          <Card.Content>
            <Text variant="titleLarge" style={styles.bannerTitle}>
              🎉 First Order Discount
            </Text>
            <Text variant="bodyMedium" style={styles.bannerText}>
              Get 20% off on your first order! Use code: FLAVORY20
            </Text>
            <Chip
              mode="flat"
              style={styles.promoChip}
              textStyle={styles.promoText}
            >
              FLAVORY20
            </Chip>
          </Card.Content>
        </Card>

        <View style={styles.bottomSpacer} />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  header: {
    padding: spacing.lg,
    backgroundColor: colors.surface,
  },
  greeting: {
    color: colors.textSecondary,
  },
  title: {
    fontWeight: "bold",
    marginTop: spacing.xs,
  },
  searchContainer: {
    padding: spacing.md,
  },
  searchBar: {
    backgroundColor: colors.surface,
    borderRadius: borderRadius.md,
  },
  section: {
    marginTop: spacing.md,
  },
  sectionHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: spacing.md,
    marginBottom: spacing.sm,
  },
  sectionTitle: {
    fontWeight: "600",
  },
  seeAll: {
    color: colors.primary,
  },
  categoriesContainer: {
    paddingHorizontal: spacing.md,
    gap: spacing.sm,
  },
  categoryCard: {
    backgroundColor: colors.surface,
    borderRadius: borderRadius.md,
    padding: spacing.md,
    alignItems: "center",
    justifyContent: "center",
    minWidth: 80,
    ...shadows.sm,
  },
  categoryIcon: {
    fontSize: 32,
    marginBottom: spacing.xs,
  },
  categoryName: {
    fontWeight: "500",
  },
  dishCard: {
    marginHorizontal: spacing.md,
    marginBottom: spacing.sm,
    ...shadows.sm,
  },
  dishContent: {
    flexDirection: "row",
    padding: spacing.md,
  },
  dishImage: {
    width: 80,
    height: 80,
    borderRadius: borderRadius.md,
    backgroundColor: colors.surface,
    justifyContent: "center",
    alignItems: "center",
    marginRight: spacing.md,
  },
  dishEmoji: {
    fontSize: 40,
  },
  dishInfo: {
    flex: 1,
    justifyContent: "space-between",
  },
  dishName: {
    fontWeight: "600",
  },
  cookName: {
    color: colors.textSecondary,
    marginTop: spacing.xs,
  },
  dishFooter: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginTop: spacing.sm,
  },
  rating: {
    flexDirection: "row",
    alignItems: "center",
  },
  ratingIcon: {
    fontSize: 14,
    marginRight: spacing.xs,
  },
  ratingText: {
    fontWeight: "600",
  },
  price: {
    fontWeight: "bold",
    color: colors.primary,
  },
  banner: {
    margin: spacing.md,
    backgroundColor: colors.primaryLight,
    ...shadows.md,
  },
  bannerTitle: {
    fontWeight: "bold",
    color: colors.white,
  },
  bannerText: {
    color: colors.white,
    marginTop: spacing.sm,
    marginBottom: spacing.md,
  },
  promoChip: {
    backgroundColor: colors.white,
    alignSelf: "flex-start",
  },
  promoText: {
    color: colors.primary,
    fontWeight: "bold",
  },
  bottomSpacer: {
    height: spacing.lg,
  },
});

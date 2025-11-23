package com.flavory.dishservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "dishes", indexes = {
        @Index(name = "idx_cook_id", columnList = "cook_id"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_available", columnList = "available")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "cook_id")
    private Long cookId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DishCategory category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_allergens", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "allergen", length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Allergen> allergens = new HashSet<>();

    @Column(nullable = false, name = "preparation_time")
    private Integer preparationTime;

    @Column(nullable = false, name = "serving_size")
    private Integer servingSize;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "dish_images", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "image_url", length = 500)
    @OrderColumn(name = "image_order")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @Column(nullable = false, name = "current_stock")
    @Builder.Default
    private Integer currentStock = 0;

    @Column(name = "max_daily_stock")
    @Builder.Default
    private Integer maxDailyStock = 0;

    @Column(nullable = false, name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(nullable = false, name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(nullable = false, name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(nullable = false, name = "total_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_tags", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Embedded
    private NutritionInfo nutritionInfo;

    @Column(nullable = false, name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "deactivation_reason", length = 500)
    private String deactivationReason;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void decreaseStock(Integer quantity) {
        if (this.currentStock < quantity) {
            throw new IllegalStateException(
                    String.format("Niewystarczający stan. Dostępne: %d, Zamówione: %d",
                            this.currentStock, quantity)
            );
        }
        this.currentStock -= quantity;

        if (this.currentStock == 0) {
            this.available = false;
        }
    }

    public void increaseStock(Integer quantity) {
        this.currentStock += quantity;

        if (this.currentStock > 0 && this.isActive) {
            this.available = true;
        }
    }

    public void recordOrder(Integer quantity, BigDecimal orderAmount) {
        decreaseStock(quantity);
        this.totalOrders += 1;
        this.totalRevenue = this.totalRevenue.add(orderAmount);
    }

    public void updateRating(BigDecimal newRating) {
        BigDecimal totalRatingSum = this.averageRating
                .multiply(BigDecimal.valueOf(this.totalRatings));
        totalRatingSum = totalRatingSum.add(newRating);
        this.totalRatings += 1;
        this.averageRating = totalRatingSum
                .divide(BigDecimal.valueOf(this.totalRatings), 2, BigDecimal.ROUND_HALF_UP);
    }

    public boolean hasStock() {
        return this.currentStock != null && this.currentStock > 0;
    }

    public boolean canBeOrdered() {
        return this.isActive && this.available && hasStock();
    }

    @Getter
    public enum DishCategory {
        APPETIZER("Przystawki"),
        SOUP("Zupy"),
        MAIN_COURSE("Dania główne"),
        DESSERT("Desery"),
        SALAD("Sałatki"),
        BEVERAGE("Napoje"),
        BREAKFAST("Śniadania"),
        SNACK("Przekąski"),
        BREAD("Pieczywo"),
        PRESERVES("Przetwory");

        private final String displayName;

        DishCategory(String displayName) {
            this.displayName = displayName;
        }

    }

    @Getter
    public enum Allergen {
        GLUTEN("Gluten"),
        DAIRY("Nabiał"),
        EGGS("Jaja"),
        FISH("Ryby"),
        SHELLFISH("Skorupiaki"),
        TREE_NUTS("Orzechy"),
        PEANUTS("Orzeszki ziemne"),
        SOYBEANS("Soja"),
        SESAME("Sezam"),
        CELERY("Seler"),
        MUSTARD("Gorczyca"),
        LUPIN("Łubin"),
        SULFITES("Siarczyny"),
        MOLLUSCS("Mięczaki");

        private final String displayName;

        Allergen(String displayName) {
            this.displayName = displayName;
        }

    }
}
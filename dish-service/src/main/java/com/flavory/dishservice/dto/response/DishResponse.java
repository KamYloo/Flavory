package com.flavory.dishservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flavory.dishservice.entity.Dish;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DishResponse {
    private Long id;
    private Long cookId;
    private String name;
    private String description;
    private BigDecimal price;
    private Dish.DishCategory category;
    private String categoryDisplayName;
    private Set<Dish.Allergen> allergens;
    private Set<String> allergenDisplayNames;
    private Integer preparationTime;
    private Integer servingSize;
    private List<String> images;
    private Boolean available;
    private Boolean featured;
    private Integer currentStock;
    private Integer maxDailyStock;
    private BigDecimal averageRating;
    private Integer totalRatings;
    private Integer totalOrders;
    private Set<String> tags;
    private NutritionInfoResponse nutritionInfo;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
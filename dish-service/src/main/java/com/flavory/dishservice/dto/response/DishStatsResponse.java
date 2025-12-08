package com.flavory.dishservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishStatsResponse {
    private Long totalDishes;
    private Long activeDishes;
    private Long availableDishes;
    private Long outOfStockDishes;
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private BigDecimal averageRating;
    private BigDecimal averagePrice;
}

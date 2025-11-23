package com.flavory.dishservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionInfo {

    @Column(name = "calories", precision = 7, scale = 2)
    private BigDecimal calories;

    @Column(name = "protein", precision = 6, scale = 2)
    private BigDecimal protein;

    @Column(name = "carbohydrates", precision = 6, scale = 2)
    private BigDecimal carbohydrates;

    @Column(name = "fats", precision = 6, scale = 2)
    private BigDecimal fats;

    @Column(name = "fiber", precision = 6, scale = 2)
    private BigDecimal fiber;

    @Column(name = "sodium", precision = 6, scale = 2)
    private BigDecimal sodium;

    @Column(name = "sugar", precision = 6, scale = 2)
    private BigDecimal sugar;
}
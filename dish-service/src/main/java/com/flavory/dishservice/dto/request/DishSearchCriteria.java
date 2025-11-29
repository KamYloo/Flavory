package com.flavory.dishservice.dto.request;

import com.flavory.dishservice.entity.Dish;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishSearchCriteria {

    private String searchTerm;

    private Dish.DishCategory category;

    @DecimalMin(value = "0.0", message = "Minimalna cena nie może być ujemna")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "Maksymalna cena nie może być ujemna")
    private BigDecimal maxPrice;

    private Set<Dish.Allergen> excludedAllergens;

    @Min(value = 1, message = "Maksymalny czas przygotowania musi być większy od 0")
    @Max(value = 480, message = "Maksymalny czas przygotowania nie może przekroczyć 8 godzin")
    private Integer maxPreparationTime;

    @DecimalMin(value = "0.0", message = "Minimalna ocena nie może być ujemna")
    @DecimalMin(value = "5.0", message = "Minimalna ocena nie może przekroczyć 5")
    private BigDecimal minRating;

    private Boolean onlyFeatured;

    private Boolean onlyAvailable;

    private String cookId;

    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
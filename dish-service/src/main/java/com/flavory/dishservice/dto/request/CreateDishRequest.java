package com.flavory.dishservice.dto.request;

import com.flavory.dishservice.entity.Dish;
import com.flavory.dishservice.validation.ValidPrice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
public class CreateDishRequest {

    @NotBlank(message = "Nazwa dania jest wymagana")
    @Size(min = 3, max = 200, message = "Nazwa dania musi mieć od 3 do 200 znaków")
    private String name;

    @NotBlank(message = "Opis jest wymagany")
    @Size(min = 20, max = 2000, message = "Opis musi mieć od 20 do 2000 znaków")
    private String description;

    @NotNull(message = "Cena jest wymagana")
    @ValidPrice
    private BigDecimal price;

    @NotNull(message = "Kategoria jest wymagana")
    private Dish.DishCategory category;

    private Set<Dish.Allergen> allergens;

    @NotNull(message = "Czas przygotowania jest wymagany")
    @Min(value = 5, message = "Czas przygotowania musi wynosić minimum 5 minut")
    @Max(value = 480, message = "Czas przygotowania nie może przekroczyć 8 godzin")
    private Integer preparationTime;

    @NotNull(message = "Rozmiar porcji jest wymagany")
    @Min(value = 1, message = "Rozmiar porcji musi wynosić minimum 1")
    @Max(value = 20, message = "Rozmiar porcji nie może przekroczyć 20")
    private Integer servingSize;

    @Min(value = 0, message = "Stan magazynowy nie może być ujemny")
    @Max(value = 100, message = "Stan magazynowy nie może przekroczyć 100")
    private Integer currentStock;

    @Min(value = 0, message = "Maksymalny dzienny stan nie może być ujemny")
    @Max(value = 100, message = "Maksymalny dzienny stan nie może przekroczyć 100")
    private Integer maxDailyStock;

    private Set<@NotBlank @Size(max = 50) String> tags;

    @Valid
    private NutritionInfoRequest nutritionInfo;
}
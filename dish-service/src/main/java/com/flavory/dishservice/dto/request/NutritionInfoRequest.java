package com.flavory.dishservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionInfoRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "Kalorie muszą być większe od 0")
    @Digits(integer = 5, fraction = 2, message = "Nieprawidłowy format kalori")
    private BigDecimal calories;

    @DecimalMin(value = "0.0", message = "Białko nie może być ujemne")
    @Digits(integer = 4, fraction = 2, message = "Nieprawidłowy format białka")
    private BigDecimal protein;

    @DecimalMin(value = "0.0", message = "Węglowodany nie mogą być ujemne")
    @Digits(integer = 4, fraction = 2, message = "Nieprawidłowy format węglowodanów")
    private BigDecimal carbohydrates;

    @DecimalMin(value = "0.0", message = "Tłuszcze nie mogą być ujemne")
    @Digits(integer = 4, fraction = 2, message = "Nieprawidłowy format tłuszczów")
    private BigDecimal fats;

    @DecimalMin(value = "0.0", message = "Błonnik nie może być ujemny")
    @Digits(integer = 4, fraction = 2, message = "Nieprawidłowy format błonnika")
    private BigDecimal fiber;

    @DecimalMin(value = "0.0", message = "Sód nie może być ujemny")
    @Digits(integer = 4, fraction = 2, message = "Nieprawidłowy format sodu")
    private BigDecimal sodium;

    @DecimalMin(value = "0.0", message = "Cukier nie może być ujemny")
    @Digits(integer = 4, fraction = 2, message = "Nieprawidłowy format cukru")
    private BigDecimal sugar;
}
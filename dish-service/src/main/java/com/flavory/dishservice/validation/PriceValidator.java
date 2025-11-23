package com.flavory.dishservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PriceValidator implements ConstraintValidator<ValidPrice, BigDecimal> {

    @Value("${app.business.min-dish-price:5.00}")
    private BigDecimal minPrice;

    @Value("${app.business.max-dish-price:500.00}")
    private BigDecimal maxPrice;

    @Override
    public boolean isValid(BigDecimal price, ConstraintValidatorContext context) {
        if (price == null) {
            return true;
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Cena musi być większa od 0"
            ).addConstraintViolation();
            return false;
        }

        if (price.compareTo(minPrice) < 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Cena nie może być mniejsza niż %.2f PLN", minPrice)
            ).addConstraintViolation();
            return false;
        }

        if (price.compareTo(maxPrice) > 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Cena nie może przekroczyć %.2f PLN", maxPrice)
            ).addConstraintViolation();
            return false;
        }

        if (price.scale() > 2) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Cena może mieć maksymalnie 2 miejsca po przecinku"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
package com.flavory.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PostalCodeValidator implements ConstraintValidator<ValidPostalCode, String> {

    private static final String POSTAL_CODE_PATTERN = "^\\d{2}-\\d{3}$";

    @Override
    public boolean isValid(String postalCode, ConstraintValidatorContext context) {
        if (postalCode == null || postalCode.isEmpty()) {
            return false;
        }
        return postalCode.matches(POSTAL_CODE_PATTERN);
    }
}
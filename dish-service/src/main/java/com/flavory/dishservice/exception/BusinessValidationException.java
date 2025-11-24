package com.flavory.dishservice.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BusinessValidationException extends DishServiceException {
    private final Map<String, String> validationErrors;

    public BusinessValidationException(String field, String error) {
        super("Błąd walidacji biznesowej", "BUSINESS_VALIDATION_ERROR");
        this.validationErrors = new HashMap<>();
        this.validationErrors.put(field, error);
    }

    public BusinessValidationException(Map<String, String> validationErrors) {
        super("Błąd walidacji biznesowej", "BUSINESS_VALIDATION_ERROR");
        this.validationErrors = validationErrors;
    }
}
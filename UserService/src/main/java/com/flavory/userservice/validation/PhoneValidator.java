package com.flavory.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private static final String PHONE_PATTERN = "^(\\+48)?[0-9]{9}$";

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.isEmpty()) {
            return true;
        }
        return phone.matches(PHONE_PATTERN);
    }
}
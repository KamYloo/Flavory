package com.flavory.userservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostalCodeValidator.class)
@Documented
public @interface ValidPostalCode {
    String message() default "Invalid postal code (format: XX-XXX)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
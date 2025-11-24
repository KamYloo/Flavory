package com.flavory.dishservice.exception;

import lombok.Getter;

@Getter
public abstract class DishServiceException extends RuntimeException {
    private final String errorCode;

    protected DishServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
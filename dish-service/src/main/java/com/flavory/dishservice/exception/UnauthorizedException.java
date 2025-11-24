package com.flavory.dishservice.exception;

public class UnauthorizedException extends DishServiceException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED_ACCESS");
    }
}
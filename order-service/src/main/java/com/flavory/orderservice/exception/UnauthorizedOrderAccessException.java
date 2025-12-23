package com.flavory.orderservice.exception;

public class UnauthorizedOrderAccessException extends RuntimeException {
    public UnauthorizedOrderAccessException() {
        super("You are not authorized to access this order");
    }

    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}

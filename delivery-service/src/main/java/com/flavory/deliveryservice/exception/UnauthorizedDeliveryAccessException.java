package com.flavory.deliveryservice.exception;

public class UnauthorizedDeliveryAccessException extends RuntimeException {
    public UnauthorizedDeliveryAccessException() {
        super("You are not authorized to access this delivery");
    }

    public UnauthorizedDeliveryAccessException(String message) {
        super(message);
    }
}
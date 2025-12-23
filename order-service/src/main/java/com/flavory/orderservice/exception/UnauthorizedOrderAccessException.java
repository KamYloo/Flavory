package com.flavory.orderservice.exception;

public class UnauthorizedOrderAccessException extends RuntimeException {
    public UnauthorizedOrderAccessException() {
        super("Nie masz uprawnień dostępu do tego zamówienia");
    }

    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}

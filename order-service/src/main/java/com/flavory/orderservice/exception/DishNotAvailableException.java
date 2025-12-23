package com.flavory.orderservice.exception;

public class DishNotAvailableException extends RuntimeException {
    public DishNotAvailableException(String dishName) {
        super("Danie'" + dishName + "' nie jest dostępne do zamówienia");
    }

    public DishNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

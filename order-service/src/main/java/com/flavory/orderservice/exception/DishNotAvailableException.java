package com.flavory.orderservice.exception;

public class DishNotAvailableException extends RuntimeException {
    public DishNotAvailableException(String dishName) {
        super("Dish '" + dishName + "' is not available for ordering");
    }

    public DishNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

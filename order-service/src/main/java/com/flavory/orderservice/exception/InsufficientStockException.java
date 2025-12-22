package com.flavory.orderservice.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String dishName, Integer available, Integer requested) {
        super(String.format("Insufficient stock for dish '%s'. Available: %d, Requested: %d",
                dishName, available, requested));
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}

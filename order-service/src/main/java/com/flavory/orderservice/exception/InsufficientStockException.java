package com.flavory.orderservice.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String dishName, Integer available, Integer requested) {
        super(String.format("Niewystarczająca ilość zapasów dla dania '%s'. Dostępne: %d, Żądane: %d",
                dishName, available, requested));
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}

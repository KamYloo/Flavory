package com.flavory.dishservice.exception;

public class InsufficientStockException extends DishServiceException {
    public InsufficientStockException(Long dishId, Integer available, Integer requested) {
        super(
                String.format("Niewystarczający stan dla dania %d. Dostępne: %d, Zamówione: %d",
                        dishId, available, requested),
                "INSUFFICIENT_STOCK"
        );
    }
}
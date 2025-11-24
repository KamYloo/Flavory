package com.flavory.dishservice.exception;

public class DishNotAvailableException extends DishServiceException {
    public DishNotAvailableException(Long dishId) {
        super(
                String.format("Danie o ID %d jest obecnie niedostępne", dishId),
                "DISH_NOT_AVAILABLE"
        );
    }
}
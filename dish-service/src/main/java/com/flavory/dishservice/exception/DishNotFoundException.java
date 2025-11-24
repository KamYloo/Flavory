package com.flavory.dishservice.exception;

public class DishNotFoundException extends DishServiceException {
    public DishNotFoundException(Long dishId) {
        super(
                String.format("Danie o ID %d nie zostało znalezione", dishId),
                "DISH_NOT_FOUND"
        );
    }
}
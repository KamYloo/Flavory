package com.flavory.dishservice.exception;

public class MaxDishesLimitException extends DishServiceException {
    public MaxDishesLimitException(String cookId, Integer maxLimit) {
        super(
                String.format("Kucharz %s osiągnął maksymalny limit %d dań", cookId, maxLimit),
                "MAX_DISHES_LIMIT"
        );
    }
}
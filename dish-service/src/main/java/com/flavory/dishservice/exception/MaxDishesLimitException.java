package com.flavory.dishservice.exception;

public class MaxDishesLimitException extends DishServiceException {
    public MaxDishesLimitException(Long cookId, Integer maxLimit) {
        super(
                String.format("Kucharz %d osiągnął maksymalny limit %d dań", cookId, maxLimit),
                "MAX_DISHES_LIMIT"
        );
    }
}
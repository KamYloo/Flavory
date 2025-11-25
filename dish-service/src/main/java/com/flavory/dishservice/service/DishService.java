package com.flavory.dishservice.service;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.response.DishResponse;

import java.util.List;

public interface DishService {
    DishResponse createDish(CreateDishRequest request, String cookId, List<String> imageUrls);
    DishResponse getDishById(Long dishId);
    DishResponse getDishByIdForCook(Long dishId, String cookId);
}

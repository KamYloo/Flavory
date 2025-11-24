package com.flavory.dishservice.service;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.response.DishResponse;

public interface DishService {
    DishResponse createDish(CreateDishRequest request, Long cookId);
}

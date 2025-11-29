package com.flavory.dishservice.service;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.request.DishSearchCriteria;
import com.flavory.dishservice.dto.response.DishResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DishService {
    DishResponse createDish(CreateDishRequest request, String cookId, List<String> imageUrls);
    DishResponse getDishById(Long dishId);
    DishResponse getDishByIdForCook(Long dishId, String cookId);
    Page<DishResponse> getDishesByCook(String cookId, Pageable pageable);
    Page<DishResponse> getFeaturedDishes(Pageable pageable);
    Page<DishResponse> getTopRatedDishes(Pageable pageable);
    Page<DishResponse> searchDishes(DishSearchCriteria criteria, Pageable pageable);
}

package com.flavory.dishservice.service;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.request.DishSearchCriteria;
import com.flavory.dishservice.dto.request.UpdateDishRequest;
import com.flavory.dishservice.dto.request.UpdateStockRequest;
import com.flavory.dishservice.dto.response.DishResponse;
import com.flavory.dishservice.dto.response.DishStatsResponse;
import com.flavory.dishservice.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface DishService {
    DishResponse createDish(CreateDishRequest request, String cookId, List<String> imageUrls);
    DishResponse updateDish(Long dishId, UpdateDishRequest request, String cookId, List<MultipartFile> newImages);
    DishResponse getDishById(Long dishId);
    DishResponse getDishByIdForCook(Long dishId, String cookId);
    Page<DishResponse> getDishesByCook(String cookId, Pageable pageable);
    Page<DishResponse> getFeaturedDishes(Pageable pageable);
    Page<DishResponse> getTopRatedDishes(Pageable pageable);
    Page<DishResponse> getAllAvailableDishes(Pageable pageable);
    Page<DishResponse> getDishesByCategory(Dish.DishCategory category, Pageable pageable);
    Page<DishResponse> searchDishes(DishSearchCriteria criteria, Pageable pageable);
    DishResponse updateStock(Long dishId, UpdateStockRequest request, String cookId);
    void decreaseStock(Long dishId, Integer quantity);
    DishResponse toggleAvailability(Long dishId, String cookId);
    void deleteDish(Long dishId, String cookId);
    DishStatsResponse getCookStatistics(String cookId);
    void updateOrderStats(Long dishId, BigDecimal itemTotal);
    void updateDishRating(Long dishId, BigDecimal rating);
}

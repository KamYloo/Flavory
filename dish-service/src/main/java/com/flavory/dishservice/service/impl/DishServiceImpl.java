package com.flavory.dishservice.service.impl;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.response.DishResponse;
import com.flavory.dishservice.entity.Dish;
import com.flavory.dishservice.exception.BusinessValidationException;
import com.flavory.dishservice.exception.MaxDishesLimitException;
import com.flavory.dishservice.mapper.DishMapper;
import com.flavory.dishservice.repository.DishRepository;
import com.flavory.dishservice.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;

    @Value("${app.business.max-dishes-per-cook:50}")
    private Integer maxDishesPerCook;

    @Override
    @Transactional
    public DishResponse createDish(CreateDishRequest request, Long cookId) {
        validateDishCreation(request, cookId);

        Dish dish = dishMapper.toEntity(request);
        dish.setCookId(cookId);

        if (request.getCurrentStock() != null) {
            dish.setCurrentStock(request.getCurrentStock());
        }

        if (request.getMaxDailyStock() != null) {
            dish.setMaxDailyStock(request.getMaxDailyStock());
        }

        Dish savedDish = dishRepository.save(dish);
        return dishMapper.toResponse(savedDish);
    }

    private void validateDishCreation(CreateDishRequest request, Long cookId) {
        Long currentDishCount = dishRepository.countActiveDishesForCook(cookId);

        if (currentDishCount >= maxDishesPerCook) {
            throw new MaxDishesLimitException(cookId, maxDishesPerCook);
        }

        if (dishRepository.existsByCookIdAndName(cookId, request.getName())) {
            throw new BusinessValidationException(
                    "name",
                    "Danie o tej nazwie już istnieje w Twoim menu"
            );
        }
    }
}

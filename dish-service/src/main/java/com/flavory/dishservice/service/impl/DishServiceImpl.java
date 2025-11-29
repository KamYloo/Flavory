package com.flavory.dishservice.service.impl;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.request.DishSearchCriteria;
import com.flavory.dishservice.dto.response.DishResponse;
import com.flavory.dishservice.entity.Dish;
import com.flavory.dishservice.exception.BusinessValidationException;
import com.flavory.dishservice.exception.DishNotFoundException;
import com.flavory.dishservice.exception.MaxDishesLimitException;
import com.flavory.dishservice.mapper.DishMapper;
import com.flavory.dishservice.repository.DishRepository;
import com.flavory.dishservice.service.DishService;
import com.flavory.dishservice.specification.DishSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;

    @Value("${app.business.max-dishes-per-cook:50}")
    private Integer maxDishesPerCook;

    @Override
    @Transactional
    public DishResponse createDish(CreateDishRequest request, String cookId, List<String> imageUrls) {
        validateDishCreation(request, cookId);

        Dish dish = dishMapper.toEntity(request);
        dish.setCookId(cookId);
        dish.setImages(imageUrls);

        Dish savedDish = dishRepository.save(dish);
        return dishMapper.toResponse(savedDish);
    }

    @Override
    @Transactional(readOnly = true)
    public DishResponse getDishById(Long dishId) {
        Dish dish = dishRepository.findByIdAndIsActiveTrue(dishId)
                .orElseThrow(() -> new DishNotFoundException(dishId));
        return dishMapper.toResponse(dish);
    }

    @Override
    @Transactional(readOnly = true)
    public DishResponse getDishByIdForCook(Long dishId, String cookId) {
        Dish dish = dishRepository.findByIdAndCookId(dishId, cookId)
                .orElseThrow(() -> new DishNotFoundException(dishId));

        return dishMapper.toResponse(dish);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DishResponse> getDishesByCook(String cookId, Pageable pageable) {
        return dishRepository.findByCookIdAndIsActiveTrue(cookId, pageable)
                .map(dishMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DishResponse> getFeaturedDishes(Pageable pageable) {
        return dishRepository.findFeaturedDishes(pageable)
                .map(dishMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DishResponse> getTopRatedDishes(Pageable pageable) {
        return dishRepository.findTopRatedDishes(pageable)
                .map(dishMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DishResponse> getAllAvailableDishes(Pageable pageable) {
        return dishRepository.findAllAvailableDishes(pageable)
                .map(dishMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DishResponse> getDishesByCategory(Dish.DishCategory category, Pageable pageable) {
        return dishRepository.findByCategory(category, pageable)
                .map(dishMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DishResponse> searchDishes(DishSearchCriteria criteria, Pageable pageable) {
        Specification<Dish> spec = DishSpecification.searchWithFilters(
                criteria.getSearchTerm(),
                criteria.getCategory(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getExcludedAllergens(),
                criteria.getMaxPreparationTime(),
                criteria.getOnlyFeatured()
        );

        if (criteria.getMinRating() != null) {
            spec = spec.and(DishSpecification.hasMinimumRating(criteria.getMinRating()));
        }

        if (criteria.getCookId() != null) {
            spec = spec.and(DishSpecification.hasCookId(criteria.getCookId()));
        }

        return dishRepository.findAll(spec, pageable)
                .map(dishMapper::toResponse);
    }

    private void validateDishCreation(CreateDishRequest request, String cookId) {
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

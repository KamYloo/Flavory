package com.flavory.dishservice.service.impl;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.request.DishSearchCriteria;
import com.flavory.dishservice.dto.request.UpdateDishRequest;
import com.flavory.dishservice.dto.request.UpdateStockRequest;
import com.flavory.dishservice.dto.response.DishResponse;
import com.flavory.dishservice.dto.response.DishStatsResponse;
import com.flavory.dishservice.entity.Dish;
import com.flavory.dishservice.event.outbound.DishAvailabilityChangedEvent;
import com.flavory.dishservice.event.outbound.DishCreatedEvent;
import com.flavory.dishservice.event.outbound.DishDeletedEvent;
import com.flavory.dishservice.event.outbound.DishUpdatedEvent;
import com.flavory.dishservice.exception.*;
import com.flavory.dishservice.mapper.DishMapper;
import com.flavory.dishservice.messaging.publisher.DishEventPublisher;
import com.flavory.dishservice.repository.DishRepository;
import com.flavory.dishservice.service.DishService;
import com.flavory.dishservice.service.FileStorageService;
import com.flavory.dishservice.specification.DishSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;
    private final FileStorageService fileStorageService;
    private final DishEventPublisher eventPublisher;

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
        publishDishCreatedEvent(savedDish);
        return dishMapper.toResponse(savedDish);
    }

    @Override
    @Transactional
    public DishResponse updateDish(Long dishId, UpdateDishRequest request, String cookId, List<MultipartFile> newImages) {
        Dish dish = dishRepository.findByIdAndCookId(dishId, cookId)
                .orElseThrow(() -> new DishNotFoundException(dishId));

        if (request.getName() != null && !request.getName().equals(dish.getName())) {
            if (dishRepository.existsByCookIdAndNameAndIdNot(cookId, request.getName(), dishId)) {
                throw new BusinessValidationException(
                        "name",
                        "Danie o tej nazwie już istnieje"
                );
            }
        }

        dishMapper.updateEntityFromRequest(request, dish);

        if (newImages != null && !newImages.isEmpty()) {
            List<String> oldImageUrls = dish.getImages();
            List<String> newImageUrls = fileStorageService.updateFiles(newImages, oldImageUrls);
            dish.setImages(newImageUrls);
        }

        Dish updatedDish = dishRepository.save(dish);
        publishDishUpdatedEvent(updatedDish);
        return dishMapper.toResponse(updatedDish);
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

    @Override
    @Transactional
    public DishResponse updateStock(Long dishId, UpdateStockRequest request, String cookId) {
        Dish dish = dishRepository.findByIdAndCookId(dishId, cookId)
                .orElseThrow(() -> new DishNotFoundException(dishId));

        boolean wasAvailable = dish.getAvailable();
        dish.setCurrentStock(request.getCurrentStock());

        if (request.getMaxDailyStock() != null) {
            dish.setMaxDailyStock(request.getMaxDailyStock());
        }

        if (request.getCurrentStock() > 0 && dish.getIsActive()) {
            dish.setAvailable(true);
        } else if (request.getCurrentStock() == 0) {
            dish.setAvailable(false);
        }

        Dish updatedDish = dishRepository.save(dish);

        if (wasAvailable != dish.getAvailable()) {
            publishDishAvailabilityChangedEvent(updatedDish);
        }

        return dishMapper.toResponse(updatedDish);
    }

    @Override
    public void decreaseStock(Long dishId, Integer quantity) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException(dishId));

        if (!dish.canBeOrdered()) {
            throw new DishNotAvailableException(dishId);
        }

        if (dish.getCurrentStock() < quantity) {
            throw new InsufficientStockException(dishId, dish.getCurrentStock(), quantity);
        }

        dish.decreaseStock(quantity);
        dishRepository.save(dish);

        if (!dish.getAvailable()) {
            publishDishAvailabilityChangedEvent(dish);
        }
    }

    @Override
    @Transactional
    public DishResponse toggleAvailability(Long dishId, String cookId) {
        Dish dish = dishRepository.findByIdAndCookId(dishId, cookId)
                .orElseThrow(() -> new DishNotFoundException(dishId));

        dish.setAvailable(!dish.getAvailable());
        Dish updatedDish = dishRepository.save(dish);

        publishDishAvailabilityChangedEvent(updatedDish);

        return dishMapper.toResponse(updatedDish);
    }

    @Override
    @Transactional
    public void deleteDish(Long dishId, String cookId) {
        Dish dish = dishRepository.findByIdAndCookId(dishId, cookId)
                .orElseThrow(() -> new DishNotFoundException(dishId));

        if (dish.getImages() != null && !dish.getImages().isEmpty()) {
            fileStorageService.deleteFiles(dish.getImages());
            dish.setImages(new ArrayList<>());
        }

        dish.setIsActive(false);
        dish.setAvailable(false);
        dish.setDeactivationReason("Usunięte przez kucharza");
        dishRepository.save(dish);
        publishDishDeletedEvent(dish);
    }

    @Override
    @Transactional(readOnly = true)
    public DishStatsResponse getCookStatistics(String cookId) {
        return DishStatsResponse.builder()
                .totalDishes(dishRepository.countTotalDishes(cookId))
                .activeDishes(dishRepository.countActiveDishesForCook(cookId))
                .availableDishes(dishRepository.countAvailableDishes(cookId))
                .outOfStockDishes(
                        dishRepository.countTotalDishes(cookId) -
                                dishRepository.countAvailableDishes(cookId)
                )
                .totalRevenue(dishRepository.getTotalRevenue(cookId))
                .totalOrders(dishRepository.getTotalOrders(cookId).intValue())
                .averageRating(dishRepository.getAverageRating(cookId))
                .averagePrice(dishRepository.getAveragePrice(cookId))
                .build();
    }

    @Override
    public void updateOrderStats(Long dishId, BigDecimal itemTotal) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException(dishId));

        dish.updateOrderStats(itemTotal);
        dishRepository.save(dish);
    }

    @Override
    public void updateDishRating(Long dishId, BigDecimal rating) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException(dishId));
        dish.updateRating(rating);
        dishRepository.save(dish);
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

    private void publishDishCreatedEvent(Dish dish) {
        DishCreatedEvent event = DishCreatedEvent.builder()
                .dishId(dish.getId())
                .cookId(dish.getCookId())
                .dishName(dish.getName())
                .price(dish.getPrice())
                .category(dish.getCategory().name())
                .available(dish.getAvailable())
                .currentStock(dish.getCurrentStock())
                .createdAt(dish.getCreatedAt())
                .build();

        eventPublisher.publishDishCreated(event);
    }

    private void publishDishUpdatedEvent(Dish dish) {
        DishUpdatedEvent event = DishUpdatedEvent.builder()
                .dishId(dish.getId())
                .cookId(dish.getCookId())
                .dishName(dish.getName())
                .price(dish.getPrice())
                .available(dish.getAvailable())
                .currentStock(dish.getCurrentStock())
                .updatedAt(dish.getUpdatedAt())
                .build();

        eventPublisher.publishDishUpdated(event);
    }

    private void publishDishDeletedEvent(Dish dish) {
        DishDeletedEvent event = DishDeletedEvent.builder()
                .dishId(dish.getId())
                .cookId(dish.getCookId())
                .deletedAt(LocalDateTime.now())
                .build();

        eventPublisher.publishDishDeleted(event);
    }

    private void publishDishAvailabilityChangedEvent(Dish dish) {
        DishAvailabilityChangedEvent event = DishAvailabilityChangedEvent.builder()
                .dishId(dish.getId())
                .cookId(dish.getCookId())
                .available(dish.getAvailable())
                .currentStock(dish.getCurrentStock())
                .reason(dish.getCurrentStock() == 0 ? "OUT_OF_STOCK" : "MANUAL_TOGGLE")
                .changedAt(LocalDateTime.now())
                .build();

        eventPublisher.publishDishAvailabilityChanged(event);
    }
}

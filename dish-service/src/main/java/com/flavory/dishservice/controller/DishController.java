package com.flavory.dishservice.controller;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.request.DishSearchCriteria;
import com.flavory.dishservice.dto.request.UpdateDishRequest;
import com.flavory.dishservice.dto.request.UpdateStockRequest;
import com.flavory.dishservice.dto.response.ApiResponse;
import com.flavory.dishservice.dto.response.DishResponse;
import com.flavory.dishservice.entity.Dish;
import com.flavory.dishservice.security.JwtService;
import com.flavory.dishservice.service.DishService;
import com.flavory.dishservice.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DishResponse>> createDish(
            @Valid @ModelAttribute CreateDishRequest request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {

        String cookId = jwtService.extractAuth0Id(authentication);
        List<String> imageUrls = fileStorageService.storeFiles(images);
        DishResponse dish = dishService.createDish(request, cookId, imageUrls);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Danie zostało utworzone pomyślnie", dish));
    }

    @PutMapping("/{dishId}")
    public ResponseEntity<ApiResponse<DishResponse>> updateDish(
            @PathVariable Long dishId,
            @Valid @ModelAttribute UpdateDishRequest request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {

        String cookId = jwtService.extractAuth0Id(authentication);
        DishResponse dish = dishService.updateDish(dishId, request, cookId, images);
        return ResponseEntity.ok(ApiResponse.success("Danie zostało zaktualizowane", dish));
    }

    @GetMapping("/{dishId}")
    public ResponseEntity<ApiResponse<DishResponse>> getDishById(@PathVariable Long dishId) {
        DishResponse dish = dishService.getDishById(dishId);
        return ResponseEntity.ok(ApiResponse.success(dish));
    }

    @GetMapping("/my-dishes/{dishId}")
    public ResponseEntity<ApiResponse<DishResponse>> getMyDish(
            @PathVariable Long dishId,
            Authentication authentication) {

        String cookId = jwtService.extractAuth0Id(authentication);
        DishResponse dish = dishService.getDishByIdForCook(dishId, cookId);
        return ResponseEntity.ok(ApiResponse.success(dish));
    }

    @GetMapping("/cook/{cookId}")
    public ResponseEntity<ApiResponse<Page<DishResponse>>> getDishesByCook(
            @PathVariable String cookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DishResponse> dishes = dishService.getDishesByCook(cookId, pageable);
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<Page<DishResponse>>> getFeaturedDishes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DishResponse> dishes = dishService.getFeaturedDishes(pageable);
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<Page<DishResponse>>> getTopRatedDishes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DishResponse> dishes = dishService.getTopRatedDishes(pageable);
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<DishResponse>>> getDishesByCategory(
            @PathVariable Dish.DishCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "averageRating"));
        Page<DishResponse> dishes = dishService.getDishesByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DishResponse>>> getAllDishes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<DishResponse> dishes = dishService.getAllAvailableDishes(pageable);
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<DishResponse>>> searchDishes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Dish.DishCategory category,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) java.util.Set<Dish.Allergen> excludedAllergens,
            @RequestParam(required = false) Integer maxPreparationTime,
            @RequestParam(required = false) java.math.BigDecimal minRating,
            @RequestParam(required = false) Boolean onlyFeatured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "averageRating") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        DishSearchCriteria criteria = DishSearchCriteria.builder()
                .searchTerm(q)
                .category(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .excludedAllergens(excludedAllergens)
                .maxPreparationTime(maxPreparationTime)
                .minRating(minRating)
                .onlyFeatured(onlyFeatured)
                .build();

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<DishResponse> dishes = dishService.searchDishes(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @PatchMapping("/{dishId}/stock")
    public ResponseEntity<ApiResponse<DishResponse>> updateStock(
            @PathVariable Long dishId,
            @Valid @RequestBody UpdateStockRequest request,
            Authentication authentication) {

        String cookId = jwtService.extractAuth0Id(authentication);
        DishResponse dish = dishService.updateStock(dishId, request, cookId);
        return ResponseEntity.ok(ApiResponse.success("Stan magazynowy zaktualizowany", dish));
    }

    @DeleteMapping("/{dishId}")
    public ResponseEntity<ApiResponse<Long>> deleteDish(
            @PathVariable Long dishId,
            Authentication authentication) {

        String cookId = jwtService.extractAuth0Id(authentication);
        dishService.deleteDish(dishId, cookId);
        return ResponseEntity.ok(ApiResponse.success("Danie zostało usunięte", dishId));
    }
}

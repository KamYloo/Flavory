package com.flavory.dishservice.controller;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.response.ApiResponse;
import com.flavory.dishservice.dto.response.DishResponse;
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
}

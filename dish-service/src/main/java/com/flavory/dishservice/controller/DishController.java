package com.flavory.dishservice.controller;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.response.ApiResponse;
import com.flavory.dishservice.dto.response.DishResponse;
import com.flavory.dishservice.security.JwtService;
import com.flavory.dishservice.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<ApiResponse<DishResponse>> createDish(
            @Valid @RequestBody CreateDishRequest request,
            Authentication authentication) {

        Long cookId = jwtService.extractUserId(authentication);
        DishResponse dish = dishService.createDish(request, cookId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Danie zostało utworzone pomyślnie", dish));
    }
}

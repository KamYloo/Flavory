package com.flavory.userservice.controller;

import com.flavory.userservice.dto.response.ApiResponse;
import com.flavory.userservice.dto.response.UserResponse;
import com.flavory.userservice.security.JwtClaims;
import com.flavory.userservice.security.JwtService;
import com.flavory.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        JwtClaims claims = jwtService.extractClaims(authentication);
        UserResponse user = userService.getOrCreateUser(claims);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
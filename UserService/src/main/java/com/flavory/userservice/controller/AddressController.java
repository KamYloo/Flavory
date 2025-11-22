package com.flavory.userservice.controller;

import com.flavory.userservice.dto.request.CreateAddressRequest;
import com.flavory.userservice.dto.response.AddressResponse;
import com.flavory.userservice.dto.response.ApiResponse;
import com.flavory.userservice.security.JwtService;
import com.flavory.userservice.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @PathVariable Long userId,
            @Valid @RequestBody CreateAddressRequest request,
            Authentication authentication) {

        String auth0Id = jwtService.extractAuth0Id(authentication);
        AddressResponse address = addressService.createAddress(userId, request, auth0Id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("The address has been successfully created", address));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById (
            @PathVariable Long userId,
            @PathVariable Long addressId,
            Authentication authentication) {
        String auth0Id = jwtService.extractAuth0Id(authentication);
        AddressResponse address = addressService.getAddressById(userId, addressId, auth0Id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
}

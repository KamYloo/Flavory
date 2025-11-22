package com.flavory.userservice.controller;

import com.flavory.userservice.dto.request.CreateAddressRequest;
import com.flavory.userservice.dto.request.UpdateAddressRequest;
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

import java.util.List;

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

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(
            @PathVariable Long userId,
            Authentication authentication) {
        String auth0Id = jwtService.extractAuth0Id(authentication);
        List<AddressResponse> addresses = addressService.getUserAddresses(userId, auth0Id);

        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request,
            Authentication authentication) {

        String auth0Id = jwtService.extractAuth0Id(authentication);
        AddressResponse address = addressService.updateAddress(userId, addressId, request, auth0Id);

        return ResponseEntity.ok(ApiResponse.success("The address has been successfully updated", address));
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            Authentication authentication) {

        String auth0Id = jwtService.extractAuth0Id(authentication);
        AddressResponse address = addressService.setDefaultAddress(userId, addressId, auth0Id);

        return ResponseEntity.ok(ApiResponse.success("Address set as default", address));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            Authentication authentication) {

        String auth0Id = jwtService.extractAuth0Id(authentication);
        addressService.deleteAddress(userId, addressId, auth0Id);

        return ResponseEntity.ok(ApiResponse.success("The address has been successfully deleted", null));
    }
}


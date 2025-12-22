package com.flavory.userservice.controller;

import com.flavory.userservice.dto.response.AddressResponse;
import com.flavory.userservice.exception.UnauthorizedAccessException;
import com.flavory.userservice.security.JwtService;
import com.flavory.userservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AddressLookupControllerOauth {
    private final AddressService addressService;
    private final JwtService jwtService;

    @GetMapping("/by-auth0/{auth0Id}/default")
    public ResponseEntity<AddressResponse> getDefaultAddressByAuth0Id(
            @PathVariable String auth0Id,
            Authentication authentication) {

        String currentAuth0Id = jwtService.extractAuth0Id(authentication);
        if (!currentAuth0Id.equals(auth0Id)) {
            throw new UnauthorizedAccessException();
        }
        AddressResponse address = addressService.getDefaultAddressByAuth0Id(auth0Id);
        return ResponseEntity.ok(address);
    }
}


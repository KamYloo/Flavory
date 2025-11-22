package com.flavory.userservice.service;

import com.flavory.userservice.dto.request.CreateAddressRequest;
import com.flavory.userservice.dto.response.AddressResponse;

public interface AddressService {
    AddressResponse createAddress(Long userId, CreateAddressRequest request, String currentAuth0Id);
    AddressResponse getAddressById(Long userId, Long addressId, String currentAuth0Id);
}

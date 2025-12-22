package com.flavory.userservice.service;

import com.flavory.userservice.dto.request.CreateAddressRequest;
import com.flavory.userservice.dto.request.UpdateAddressRequest;
import com.flavory.userservice.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressResponse createAddress(Long userId, CreateAddressRequest request, String currentAuth0Id);
    AddressResponse getAddressById(Long userId, Long addressId, String currentAuth0Id);
    List<AddressResponse> getUserAddresses(Long userId, String currentAuth0Id);
    AddressResponse getDefaultAddressByAuth0Id(String auth0Id);
    AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request, String currentAuth0Id);
    AddressResponse setDefaultAddress(Long userId, Long addressId, String currentAuth0Id);
    void deleteAddress(Long userId, Long addressId, String currentAuth0Id);
}

package com.flavory.userservice.service.impl;

import com.flavory.userservice.dto.request.CreateAddressRequest;
import com.flavory.userservice.dto.response.AddressResponse;
import com.flavory.userservice.entity.Address;
import com.flavory.userservice.entity.User;
import com.flavory.userservice.exception.UnauthorizedAccessException;
import com.flavory.userservice.exception.UserNotFoundException;
import com.flavory.userservice.mapper.AddressMapper;
import com.flavory.userservice.repository.AddressRepository;
import com.flavory.userservice.repository.UserRepository;
import com.flavory.userservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponse createAddress(Long userId, CreateAddressRequest request, String currentAuth0Id) {
        User user = getUserAndValidateAccess(userId, currentAuth0Id);

        boolean isDefault = request.getIsDefault() != null && request.getIsDefault();
        if (user.getAddresses().isEmpty()) {
            isDefault = true;
        }

        if (isDefault) {
            addressRepository.clearDefaultAddress(userId);
        }

        Address address = addressMapper.toEntity(request);
        address.setUser(user);
        address.setIsDefault(isDefault);

        if (request.getCountry() == null || request.getCountry().isEmpty()) {
            address.setCountry("Poland");
        }

        Address savedAddress = addressRepository.save(address);
        return addressMapper.toResponse(savedAddress);
    }

    private User getUserAndValidateAccess(Long userId, String currentAuth0Id) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (!user.getAuth0Id().equals(currentAuth0Id)) {
            throw new UnauthorizedAccessException();
        }

        return user;
    }
}

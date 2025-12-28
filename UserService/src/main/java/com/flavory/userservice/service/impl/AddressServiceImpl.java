package com.flavory.userservice.service.impl;

import com.flavory.userservice.dto.request.CreateAddressRequest;
import com.flavory.userservice.dto.request.UpdateAddressRequest;
import com.flavory.userservice.dto.response.AddressResponse;
import com.flavory.userservice.entity.Address;
import com.flavory.userservice.entity.User;
import com.flavory.userservice.exception.AddressNotFoundException;
import com.flavory.userservice.exception.UnauthorizedAccessException;
import com.flavory.userservice.exception.UserNotFoundException;
import com.flavory.userservice.mapper.AddressMapper;
import com.flavory.userservice.repository.AddressRepository;
import com.flavory.userservice.repository.UserRepository;
import com.flavory.userservice.security.JwtService;
import com.flavory.userservice.service.AddressService;
import com.flavory.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;
    private final JwtService jwtService;

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

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long userId, Long addressId, String currentAuth0Id) {
        getUserAndValidateAccess(userId, currentAuth0Id);
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(AddressNotFoundException::new);

        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId, String currentAuth0Id) {
        getUserAndValidateAccess(userId, currentAuth0Id);

        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddressByAuth0Id(String auth0Id) {
        userRepository.findByAuth0Id(auth0Id)
                .orElseThrow(UserNotFoundException::new);

        Address address = addressRepository.findDefaultAddressByAuth0Id(auth0Id)
                .orElseThrow(AddressNotFoundException::new);

        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request, String currentAuth0Id) {
        getUserAndValidateAccess(userId, currentAuth0Id);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(AddressNotFoundException::new);

        addressMapper.updateEntityFromDto(request, address);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultAddress(userId);
            address.setIsDefault(true);
        }

        Address updatedAddress = addressRepository.save(address);
        return addressMapper.toResponse(updatedAddress);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long userId, Long addressId, String currentAuth0Id) {
        getUserAndValidateAccess(userId, currentAuth0Id);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(AddressNotFoundException::new);

        addressRepository.clearDefaultAddress(userId);
        address.setIsDefault(true);

        Address updatedAddress = addressRepository.save(address);
        return addressMapper.toResponse(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId, String currentAuth0Id) {
        getUserAndValidateAccess(userId, currentAuth0Id);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(AddressNotFoundException::new);

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.getFirst();
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }

    private User getUserAndValidateAccess(Long userId, String currentAuth0Id) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (!user.getAuth0Id().equals(currentAuth0Id)) {
            throw new UnauthorizedAccessException();
        }

        return user;
    }
}

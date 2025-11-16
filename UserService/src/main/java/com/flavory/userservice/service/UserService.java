package com.flavory.userservice.service;

import com.flavory.userservice.dto.request.UpdateUserRequest;
import com.flavory.userservice.dto.response.UserResponse;
import com.flavory.userservice.security.JwtClaims;

public interface UserService {
    UserResponse getOrCreateUser(JwtClaims claims);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UpdateUserRequest request, String currentAuth0Id);
    void deleteUser(Long id, String currentAuth0Id);
}
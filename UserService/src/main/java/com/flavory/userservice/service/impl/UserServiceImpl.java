package com.flavory.userservice.service.impl;

import com.flavory.userservice.dto.request.UpdateUserRequest;
import com.flavory.userservice.dto.response.UserResponse;
import com.flavory.userservice.entity.User;
import com.flavory.userservice.entity.enums.UserRole;
import com.flavory.userservice.entity.enums.UserStatus;
import com.flavory.userservice.exception.UserNotFoundException;
import com.flavory.userservice.mapper.UserMapper;
import com.flavory.userservice.repository.UserRepository;
import com.flavory.userservice.security.JwtClaims;
import com.flavory.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    @Transactional
    public UserResponse getOrCreateUser(JwtClaims claims) {
        return userRepository.findByAuth0Id(claims.getSub())
                .map(user -> {
                    boolean updated = updateUserFromClaims(user, claims);

                    if (updated) {
                        user = userRepository.save(user);
                    }

                    return userMapper.toResponse(user);
                })
                .orElseGet(() -> createUserFromClaims(claims));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, String currentAuth0Id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        if (!user.getAuth0Id().equals(currentAuth0Id)) {
            throw new UserNotFoundException();
        }

        userMapper.updateEntityFromDto(request, user);
        User userUpdated = userRepository.save(user);
        return userMapper.toResponse(userUpdated);
    }

    @Override
    public void deleteUser(Long id, String currentAuth0Id) {

    }


    private UserResponse createUserFromClaims(JwtClaims claims) {

        UserRole role = determineRoleFromClaims(claims);

        User newUser = User.builder()
                .auth0Id(claims.getSub())
                .email(claims.getEmail())
                .firstName(claims.getGivenName() != null ? claims.getGivenName() : "")
                .lastName(claims.getFamilyName() != null ? claims.getFamilyName() : "")
                .profileImageUrl(claims.getPicture())
                .role(role)
                .status(Boolean.TRUE.equals(claims.getEmailVerified()) ?
                        UserStatus.ACTIVE : UserStatus.PENDING_VERIFICATION)
                .isVerified(Boolean.TRUE.equals(claims.getEmailVerified()))
                .build();

        User savedUser = userRepository.save(newUser);

        return userMapper.toResponse(savedUser);
    }

    private boolean updateUserFromClaims(User user, JwtClaims claims) {
        boolean updated = false;

        if (claims.getGivenName() != null && !claims.getGivenName().equals(user.getFirstName())) {
            user.setFirstName(claims.getGivenName());
            updated = true;
        }

        if (claims.getFamilyName() != null && !claims.getFamilyName().equals(user.getLastName())) {
            user.setLastName(claims.getFamilyName());
            updated = true;
        }

        if (claims.getPicture() != null && !claims.getPicture().equals(user.getProfileImageUrl())) {
            user.setProfileImageUrl(claims.getPicture());
            updated = true;
        }

        if (Boolean.TRUE.equals(claims.getEmailVerified()) && !user.getIsVerified()) {
            user.setIsVerified(true);
            user.setStatus(UserStatus.ACTIVE);
            updated = true;
        }

        return updated;
    }

    private UserRole determineRoleFromClaims(JwtClaims claims) {
        if (claims.getRoles() != null) {
            if (claims.getRoles().contains("Cook") || claims.getRoles().contains("COOK")) {
                return UserRole.COOK;
            }
            if (claims.getRoles().contains("Admin") || claims.getRoles().contains("ADMIN")) {
                return UserRole.ADMIN;
            }
        }
        return UserRole.CUSTOMER;
    }
}

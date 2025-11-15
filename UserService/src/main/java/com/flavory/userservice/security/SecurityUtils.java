package com.flavory.userservice.security;

import com.flavory.userservice.exception.UnauthorizedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final JwtService jwtService;

    public String getCurrentAuth0Id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException();
        }
        return jwtService.extractAuth0Id(authentication);
    }

    public void validateUserAccess(String auth0Id) {
        String currentAuth0Id = getCurrentAuth0Id();
        if (!currentAuth0Id.equals(auth0Id)) {
            throw new UnauthorizedAccessException();
        }
    }
}
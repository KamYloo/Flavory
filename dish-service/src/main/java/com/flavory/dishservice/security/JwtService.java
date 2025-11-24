package com.flavory.dishservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JwtService {

    private static final String NAMESPACE = "https://flavory.com/";

    public String extractAuth0Id(Authentication authentication) {
        return getJwt(authentication).getSubject();
    }

    public Long extractUserId(Authentication authentication) {
        Jwt jwt = getJwt(authentication);

        Object userIdClaim = getClaimObject(jwt, "user_id");

        if (userIdClaim == null) {
            throw new IllegalStateException("User ID not found in token. Check Auth0 Actions configuration.");
        }

        if (userIdClaim instanceof Integer) {
            return ((Integer) userIdClaim).longValue();
        } else if (userIdClaim instanceof Long) {
            return (Long) userIdClaim;
        } else if (userIdClaim instanceof String) {
            try {
                return Long.parseLong((String) userIdClaim);
            } catch (NumberFormatException e) {
            }
        } else if (userIdClaim instanceof Double) {
            return ((Double) userIdClaim).longValue();
        }

        throw new IllegalStateException("Invalid format for 'user_id' in token: " + userIdClaim.getClass());
    }

    public String extractCookName(Authentication authentication) {
        Jwt jwt = getJwt(authentication);

        String firstName = getClaimAsString(jwt, "given_name");
        String lastName = getClaimAsString(jwt, "family_name");
        String name = getClaimAsString(jwt, "name");

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }

        return name != null ? name : "Unknown Cook";
    }

    public boolean hasRole(Authentication authentication, String role) {
        Jwt jwt = getJwt(authentication);
        List<String> roles = getClaimAsStringList(jwt, "roles");
        if (roles.isEmpty()) {
            roles = getClaimAsStringList(jwt, "permissions");
        }
        return roles.contains(role);
    }

    private Jwt getJwt(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt) {
            return (Jwt) authentication.getPrincipal();
        }
        throw new IllegalArgumentException("Authentication must be based on JWT");
    }

    private String getClaimAsString(Jwt jwt, String claimName) {
        String value = jwt.getClaimAsString(NAMESPACE + claimName);
        if (value == null) {
            value = jwt.getClaimAsString(claimName);
        }
        return value;
    }

    private Object getClaimObject(Jwt jwt, String claimName) {
        Object value = jwt.getClaims().get(NAMESPACE + claimName);
        if (value == null) {
            value = jwt.getClaims().get(claimName);
        }
        return value;
    }

    private List<String> getClaimAsStringList(Jwt jwt, String claimName) {
        List<String> value = jwt.getClaimAsStringList(NAMESPACE + claimName);
        if (value == null) {
            value = jwt.getClaimAsStringList(claimName);
        }
        return value != null ? value : List.of();
    }
}
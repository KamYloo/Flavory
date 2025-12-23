package com.flavory.orderservice.security;

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

        switch (userIdClaim) {
            case null -> throw new IllegalStateException("Nie znaleziono identyfikatora użytkownika w tokenie");
            case Integer i -> {
                return i.longValue();
            }
            case Long l -> {
                return l;
            }
            case String s -> {
                try {
                    return Long.parseLong((String) userIdClaim);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Nieprawidłowy format identyfikatora użytkownika");
                }
            }
            case Double v -> {
                return v.longValue();
            }
            default -> {
            }
        }

        throw new IllegalStateException("Nieprawidłowy format dla „user_id” w tokenie");
    }

    public String extractUserName(Authentication authentication) {
        Jwt jwt = getJwt(authentication);
        String firstName = getClaimAsString(jwt, "given_name");
        String lastName = getClaimAsString(jwt, "family_name");
        String name = getClaimAsString(jwt, "name");

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return name != null ? name : "Nieznany użytkownik";
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
        throw new IllegalArgumentException("Uwierzytelnianie musi być oparte na JWT");
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
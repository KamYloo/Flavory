package com.flavory.userservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class JwtService {

    private static final String NAMESPACE = "https://api.flavory.com/";

    public JwtClaims extractClaims(Authentication authentication) {
        Jwt jwt = getJwt(authentication);

        return JwtClaims.builder()
                .sub(jwt.getSubject())
                .email(getClaimAsString(jwt, "email"))
                .emailVerified(getClaimAsBoolean(jwt, "email_verified"))
                .givenName(getClaimAsString(jwt, "given_name"))
                .familyName(getClaimAsString(jwt, "family_name"))
                .name(getClaimAsString(jwt, "name"))
                .picture(getClaimAsString(jwt, "picture"))
                .roles(getClaimAsStringList(jwt, "roles"))
                .permissions(getClaimAsStringList(jwt, "permissions"))
                .build();
    }

    public String extractAuth0Id(Authentication authentication) {
        return getJwt(authentication).getSubject();
    }

    private String getClaimAsString(Jwt jwt, String claimName) {
        String value = jwt.getClaimAsString(NAMESPACE + claimName);
        if (value == null) {
            value = jwt.getClaimAsString(claimName);
        }
        return value;
    }

    private Boolean getClaimAsBoolean(Jwt jwt, String claimName) {
        Boolean value = jwt.getClaimAsBoolean(NAMESPACE + claimName);
        if (value == null) {
            value = jwt.getClaimAsBoolean(claimName);
        }
        return value;
    }

    private List<String> getClaimAsStringList(Jwt jwt, String claimName) {
        List<String> value = jwt.getClaimAsStringList(NAMESPACE + claimName);
        if (value == null || value.isEmpty()) {
            value = jwt.getClaimAsStringList(claimName);
        }
        return value != null ? value : List.of();
    }

    private Jwt getJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        throw new IllegalArgumentException("Incorrect authentication type");
    }
}
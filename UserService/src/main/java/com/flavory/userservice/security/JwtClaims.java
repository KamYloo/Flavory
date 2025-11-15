package com.flavory.userservice.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {
    private String sub;
    private String email;
    private Boolean emailVerified;
    private String givenName;
    private String familyName;
    private String name;
    private String picture;
    private List<String> roles;
    private List<String> permissions;
}

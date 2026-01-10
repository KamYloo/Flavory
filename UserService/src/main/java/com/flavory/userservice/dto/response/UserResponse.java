package com.flavory.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flavory.userservice.entity.enums.UserRole;
import com.flavory.userservice.entity.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String auth0Id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String profileImageUrl;
    private UserRole role;
    private UserStatus status;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
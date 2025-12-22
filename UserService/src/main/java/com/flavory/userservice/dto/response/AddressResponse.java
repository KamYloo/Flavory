package com.flavory.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressResponse {
    private Long id;
    private String street;
    private String city;
    private String postalCode;
    private String apartmentNumber;
    private String country;
    private String fullAddress;
    private Boolean isDefault;
    private String label;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private String phoneNumber;
}
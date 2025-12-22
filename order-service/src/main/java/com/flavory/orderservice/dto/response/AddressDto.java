package com.flavory.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private Long id;
    private String street;
    private String city;
    private String postalCode;
    private String apartmentNumber;
    private String country;
    private Boolean isDefault;
    private String label;
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
}
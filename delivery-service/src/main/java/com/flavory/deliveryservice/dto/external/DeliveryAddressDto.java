package com.flavory.deliveryservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddressDto {
    private String street;
    private String city;
    private String postalCode;
    private String apartmentNumber;
    private String phoneNumber;
    private String deliveryInstructions;
    private Double latitude;
    private Double longitude;
}

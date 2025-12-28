package com.flavory.deliveryservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddressResponse {
    private String street;
    private String city;
    private String postalCode;
    private String apartmentNumber;
    private String phoneNumber;
    private String contactName;
    private String instructions;
    private Double latitude;
    private Double longitude;
}

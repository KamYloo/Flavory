package com.flavory.userservice.dto.request;

import com.flavory.userservice.validation.ValidPostalCode;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAddressRequest {

    @NotBlank(message = "Street address is required")
    @Size(max = 200)
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "Postal code is required")
    @ValidPostalCode
    private String postalCode;

    @Size(max = 20)
    private String apartmentNumber;

    @Size(max = 50)
    private String country;

    private Boolean isDefault;

    @Size(max = 50)
    private String label;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;
}
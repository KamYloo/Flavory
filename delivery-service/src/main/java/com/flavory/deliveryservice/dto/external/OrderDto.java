package com.flavory.deliveryservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String customerId;
    private String cookId;
    private String status;
    private DeliveryAddressDto deliveryAddress;
}

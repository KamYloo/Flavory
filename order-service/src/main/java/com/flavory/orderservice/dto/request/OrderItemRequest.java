package com.flavory.orderservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    @NotNull(message = "ID dania jest wymagane")
    private Long dishId;

    @NotNull(message = "Ilość jest wymagana")
    @Min(value = 1, message = "Ilość musi wynosić co najmniej 1")
    private Integer quantity;
}
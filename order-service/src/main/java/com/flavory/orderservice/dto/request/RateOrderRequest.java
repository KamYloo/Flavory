package com.flavory.orderservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateOrderRequest {

    @NotNull(message = "Ocena jest wymagana")
    @DecimalMin(value = "1.0", message = "Ocena musi wynosić co najmniej 1,0")
    @DecimalMax(value = "5.0", message = "Ocena nie może przekroczyć 5,0")
    @Digits(integer = 1, fraction = 1, message = "Ocena może mieć maksymalnie 1 miejsce po przecinku")
    private BigDecimal rating;

    @NotNull(message = "Wymagane jest podanie identyfikatora dania")
    private Long dishId;
}
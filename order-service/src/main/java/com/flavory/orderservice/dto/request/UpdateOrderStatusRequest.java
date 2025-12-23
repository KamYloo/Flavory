package com.flavory.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status jest wymagany")
    @Pattern(
            regexp = "CONFIRMED|PREPARING|READY|IN_DELIVERY|DELIVERED",
            message = "Nieprawidłowy status. Dozwolone wartości: CONFIRMED, PREPARING, READY, IN_DELIVERY, DELIVERED"
    )
    private String status;
}
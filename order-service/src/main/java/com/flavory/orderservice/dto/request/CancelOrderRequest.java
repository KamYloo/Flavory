package com.flavory.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {

    @NotBlank(message = "Powód anulowania jest wymagany")
    @Size(min = 10, max = 500, message = "Powód anulowania musi zawierać od 10 do 500 znaków")
    private String reason;
}

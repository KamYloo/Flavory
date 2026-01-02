package com.flavory.paymentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmPaymentRequest {

    @NotBlank(message = "Payment Intent ID jest wymagane")
    private String paymentIntentId;
}

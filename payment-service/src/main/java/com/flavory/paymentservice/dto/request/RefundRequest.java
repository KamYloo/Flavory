package com.flavory.paymentservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {

    @NotNull(message = "Kwota zwrotu jest wymagana")
    @DecimalMin(value = "0.01", message = "Minimalna kwota zwrotu to 0.01 PLN")
    @Digits(integer = 8, fraction = 2, message = "Nieprawidłowy format kwoty")
    private BigDecimal amount;

    @NotBlank(message = "Powód zwrotu jest wymagany")
    @Size(min = 10, max = 500, message = "Powód zwrotu musi mieć od 10 do 500 znaków")
    private String reason;
}
package com.flavory.paymentservice.dto.request;

import com.flavory.paymentservice.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentIntentRequest {

    @NotNull(message = "ID zamówienia jest wymagane")
    @Positive(message = "ID zamówienia musi być dodatnie")
    private Long orderId;

    @NotBlank(message = "ID klienta jest wymagane")
    private String customerId;

    @NotBlank(message = "ID kucharza jest wymagane")
    private String cookId;

    @NotNull(message = "Kwota jest wymagana")
    @DecimalMin(value = "1.00", message = "Minimalna kwota to 1.00 PLN")
    @DecimalMax(value = "10000.00", message = "Maksymalna kwota to 10000.00 PLN")
    @Digits(integer = 8, fraction = 2, message = "Nieprawidłowy format kwoty")
    private BigDecimal amount;

    @NotNull(message = "Metoda płatności jest wymagana")
    private PaymentMethod paymentMethod;

    private String metadata;
}
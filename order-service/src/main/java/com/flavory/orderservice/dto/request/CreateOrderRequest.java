package com.flavory.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "ID kucharza jest wymagane")
    @NotBlank(message = "ID kucharza nie może być puste")
    private String cookId;

    @NotNull(message = "Zamówienie musi zawierać przynajmniej jedną pozycję")
    @NotEmpty(message = "Zamówienie musi zawierać przynajmniej jedną pozycję")
    @Size(min = 1, max = 20, message = "Zamówienie musi zawierać od 1 do 20 pozycji")
    @Valid
    private List<OrderItemRequest> items;

    @Size(max = 500, message = "Uwagi klienta nie mogą przekraczać 500 znaków")
    private String customerNotes;

    @Size(max = 500, message = "Instrukcje dostawy nie mogą przekraczać 500 znaków")
    private String deliveryInstructions;

    @NotNull(message = "Metoda płatności jest wymagana")
    @NotBlank(message = "Metoda płatności nie może być pusta")
    @Pattern(regexp = "CARD|BLIK|CASH", message = "Metoda płatności musi być jedną z: CARD, BLIK lub CASH")
    private String paymentMethod;
}
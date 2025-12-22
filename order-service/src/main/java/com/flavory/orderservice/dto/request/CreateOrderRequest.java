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

    @NotNull(message = "Cook ID is required")
    @NotBlank(message = "Cook ID cannot be blank")
    private String cookId;

    @NotNull(message = "Order must contain at least one item")
    @NotEmpty(message = "Order must contain at least one item")
    @Size(min = 1, max = 20, message = "Order must contain between 1 and 20 items")
    @Valid
    private List<OrderItemRequest> items;

    @Size(max = 500, message = "Customer notes cannot exceed 500 characters")
    private String customerNotes;

    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;

    @NotNull(message = "Payment method is required")
    @NotBlank(message = "Payment method cannot be blank")
    @Pattern(regexp = "CARD|BLIK|CASH", message = "Payment method must be CARD, BLIK, or CASH")
    private String paymentMethod;
}

package com.flavory.orderservice.event.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {
    private Long paymentId;
    private Long orderId;
    private String customerId;
    private String cookId;
    private BigDecimal amount;
    private String status;
    private String stripePaymentIntentId;
    private String failureReason;
    private LocalDateTime timestamp;
    private String eventType;
    private String eventId;
}

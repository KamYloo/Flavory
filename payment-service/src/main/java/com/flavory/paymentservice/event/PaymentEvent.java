package com.flavory.paymentservice.event;

import com.flavory.paymentservice.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    private Long paymentId;
    private Long orderId;
    private String customerId;
    private String cookId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String stripePaymentIntentId;
    private LocalDateTime timestamp;
    private PaymentEventType eventType;
}
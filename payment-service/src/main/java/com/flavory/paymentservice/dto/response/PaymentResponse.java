package com.flavory.paymentservice.dto.response;

import com.flavory.paymentservice.entity.PaymentMethod;
import com.flavory.paymentservice.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String customerId;
    private String cookId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private String stripePaymentIntentId;
    private BigDecimal platformFee;
    private BigDecimal cookPayout;
    private String cardLast4;
    private String cardBrand;
    private String failureMessage;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
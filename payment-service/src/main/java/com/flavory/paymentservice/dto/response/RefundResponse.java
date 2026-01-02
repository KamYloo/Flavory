package com.flavory.paymentservice.dto.response;

import com.flavory.paymentservice.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {
    private Long paymentId;
    private String refundId;
    private BigDecimal refundAmount;
    private String refundReason;
    private PaymentStatus status;
    private LocalDateTime refundedAt;
}
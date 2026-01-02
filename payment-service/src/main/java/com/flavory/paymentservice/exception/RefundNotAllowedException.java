package com.flavory.paymentservice.exception;

import com.flavory.paymentservice.entity.PaymentStatus;

public class RefundNotAllowedException extends PaymentException {
    public RefundNotAllowedException(Long paymentId, PaymentStatus status) {
        super(String.format("Zwrot dla płatności %d nie jest możliwy. Status: %s",
                paymentId, status.getDisplayName()));
    }

    public RefundNotAllowedException(String message) {
        super(message);
    }
}

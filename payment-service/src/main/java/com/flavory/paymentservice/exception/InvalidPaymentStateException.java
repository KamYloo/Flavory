package com.flavory.paymentservice.exception;

import com.flavory.paymentservice.entity.PaymentStatus;

public class InvalidPaymentStateException extends PaymentException {
    public InvalidPaymentStateException(Long paymentId, PaymentStatus currentStatus, String operation) {
        super(String.format("Nie można wykonać operacji '%s' dla płatności %d w statusie %s",
                operation, paymentId, currentStatus.getDisplayName()));
    }
}
package com.flavory.paymentservice.exception;

public class DuplicatePaymentException extends PaymentException {
    public DuplicatePaymentException(Long orderId) {
        super(String.format("Płatność dla zamówienia %d już istnieje", orderId));
    }
}
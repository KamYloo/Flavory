package com.flavory.paymentservice.exception;

public class PaymentNotFoundException extends PaymentException {
    public PaymentNotFoundException(Long paymentId) {
        super(String.format("Płatność o ID %d nie została znaleziona", paymentId));
    }

    public PaymentNotFoundException(String stripePaymentIntentId) {
        super(String.format("Płatność Stripe %s nie została znaleziona", stripePaymentIntentId));
    }
}

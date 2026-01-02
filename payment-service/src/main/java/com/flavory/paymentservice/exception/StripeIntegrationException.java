package com.flavory.paymentservice.exception;

public class StripeIntegrationException extends PaymentException {
    public StripeIntegrationException(String message) {
        super(message);
    }

    public StripeIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
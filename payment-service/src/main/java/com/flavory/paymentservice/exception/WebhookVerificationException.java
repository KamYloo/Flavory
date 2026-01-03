package com.flavory.paymentservice.exception;

public class WebhookVerificationException extends PaymentException {
    public WebhookVerificationException(String message) {
        super("Weryfikacja webhooka Stripe nie powiodła się: " + message);
    }

    public WebhookVerificationException(String message, Throwable cause) {
        super("Weryfikacja webhooka Stripe nie powiodła się: " + message, cause);
    }
}

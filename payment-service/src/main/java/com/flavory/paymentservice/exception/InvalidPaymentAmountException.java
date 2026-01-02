package com.flavory.paymentservice.exception;

import java.math.BigDecimal;

public class InvalidPaymentAmountException extends PaymentException {
    public InvalidPaymentAmountException(BigDecimal amount) {
        super(String.format("Nieprawidłowa kwota płatności: %s PLN. Minimalna kwota to 1.00 PLN", amount));
    }
}
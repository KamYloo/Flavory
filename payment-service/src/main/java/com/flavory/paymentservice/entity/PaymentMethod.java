package com.flavory.paymentservice.entity;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CARD("Karta płatnicza"),
    BLIK("BLIK"),
    P24("Przelewy24"),
    PAYPAL("PayPal");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
}

package com.flavory.paymentservice.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Oczekujące"),
    REQUIRES_ACTION("Wymaga akcji"),
    PROCESSING("Przetwarzanie"),
    SUCCEEDED("Zakończone sukcesem"),
    FAILED("Nieudane"),
    CANCELLED("Anulowane"),
    REFUNDED("Zwrócone");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}

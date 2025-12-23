package com.flavory.orderservice.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super("Zamówienie o ID " + orderId + " nie zostało znalezione");
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}

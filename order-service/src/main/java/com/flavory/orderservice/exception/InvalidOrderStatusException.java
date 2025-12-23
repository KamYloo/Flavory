package com.flavory.orderservice.exception;

import com.flavory.orderservice.entity.Order.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(OrderStatus from, OrderStatus to) {
        super(String.format("Nieprawidłowa zmiana statusu zamówienia z %s na %s", from, to));
    }

    public InvalidOrderStatusException(String message) {
        super(message);
    }
}

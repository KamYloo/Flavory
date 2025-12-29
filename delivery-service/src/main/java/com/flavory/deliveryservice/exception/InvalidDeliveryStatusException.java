package com.flavory.deliveryservice.exception;

import com.flavory.deliveryservice.entity.Delivery.DeliveryStatus;

public class InvalidDeliveryStatusException extends RuntimeException {
    public InvalidDeliveryStatusException(DeliveryStatus from, DeliveryStatus to) {
        super(String.format("Invalid delivery status transition from %s to %s", from, to));
    }

    public InvalidDeliveryStatusException(String message) {
        super(message);
    }
}

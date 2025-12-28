package com.flavory.deliveryservice.exception;

public class DeliveryNotFoundException extends RuntimeException {
    public DeliveryNotFoundException(Long deliveryId) {
        super("Delivery with ID " + deliveryId + " not found");
    }

    public DeliveryNotFoundException(String message) {
        super(message);
    }
}

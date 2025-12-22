package com.flavory.orderservice.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException() {
        super("No default address found for user");
    }

    public AddressNotFoundException(String message) {
        super(message);
    }
}

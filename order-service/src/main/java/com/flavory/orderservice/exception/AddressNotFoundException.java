package com.flavory.orderservice.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException() {
        super("Nie znaleziono domyślnego adresu dla użytkownika");
    }

    public AddressNotFoundException(String message) {
        super(message);
    }
}

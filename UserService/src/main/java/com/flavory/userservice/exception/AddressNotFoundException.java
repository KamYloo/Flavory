package com.flavory.userservice.exception;

public class AddressNotFoundException extends BusinessException {
    public AddressNotFoundException() {
        super(ErrorCode.ADDRESS_NOT_FOUND);
    }
}
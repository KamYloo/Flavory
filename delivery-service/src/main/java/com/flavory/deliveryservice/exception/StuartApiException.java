package com.flavory.deliveryservice.exception;

public class StuartApiException extends RuntimeException {
    public StuartApiException(String message) {
        super("Stuart API error: " + message);
    }

    public StuartApiException(String message, Throwable cause) {
        super("Stuart API error: " + message, cause);
    }
}
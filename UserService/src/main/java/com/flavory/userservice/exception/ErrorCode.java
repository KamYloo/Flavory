package com.flavory.userservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("USER_1001", "User not found"),
    USER_ALREADY_EXISTS("USER_1002", "User with the specified email already exists"),
    USER_UNAUTHORIZED_ACCESS("USER_1003", "No permissions for this use"),

    ADDRESS_NOT_FOUND("ADDRESS_2001", "Address not found"),

    AUTH_TOKEN_INVALID("AUTH_3001", "Invalid token"),

    VALIDATION_FAILED("VALIDATION_4001", "Data validation failed"),

    INTERNAL_SERVER_ERROR("GENERAL_9001", "A server error occurred");

    private final String code;
    private final String message;
}

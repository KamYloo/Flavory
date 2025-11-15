package com.flavory.userservice.exception;

public class UnauthorizedAccessException extends BusinessException {
    public UnauthorizedAccessException() {
        super(ErrorCode.USER_UNAUTHORIZED_ACCESS);
    }
}
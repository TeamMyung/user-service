package com.sparta.userservice.global.exception;

import com.sparta.userservice.global.response.ErrorCode;

public class AuthException extends RuntimeException {
    private final ErrorCode errorCode;

    public AuthException(ErrorCode errorCode) {
        super(errorCode.getDetails());
        this.errorCode = errorCode;
    }
}

package com.sparta.userservice.global.exception;

import com.sparta.userservice.global.response.ErrorCode;

public class UserException extends RuntimeException {
    private final ErrorCode errorCode;

    public UserException(ErrorCode errorCode) {
        super(errorCode.getDetails());
        this.errorCode = errorCode;
    }
}

package com.sparta.userservice.global.exception;

import com.sparta.userservice.global.response.ApiResponse;
import com.sparta.userservice.global.response.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse> handleUserException(UserException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ApiResponse<>(errorCode));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(ErrorCode.USER_BAD_REQUEST));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity
                .status(ErrorCode.USER_CONFLICT.getStatus())
                .body(new ApiResponse<>(ErrorCode.USER_CONFLICT));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity
                .status(ErrorCode.USER_INTERNAL_SERVER_ERROR.getStatus())
                .body(new ApiResponse<>(ErrorCode.USER_INTERNAL_SERVER_ERROR));
    }
}
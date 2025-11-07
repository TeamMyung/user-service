package com.sparta.userservice.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // auth
    AUTH_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "일치하는 회원 정보를 찾을 수 없습니다."),
    AUTH_INVALID_CREDENTIALS(1002, HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다."),
    AUTH_PASSWORD_MISMATCH(1003, HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    AUTH_DUPLICATED_USERNAME(1004, HttpStatus.BAD_REQUEST, "이미 존재하는 아이디입니다."),
    AUTH_DUPLICATED_EMAIL(1005, HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
    AUTH_INVALID_REFRESH_TOKEN(1006, HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    AUTH_PENDING_APPROVAL(1007, HttpStatus.UNAUTHORIZED, "계정이 승인되지 않았습니다."),

    ;

    private final int code;
    private final HttpStatus status;
    private final String details;
}

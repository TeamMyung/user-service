package com.sparta.userservice.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // auth
    AUTH_USER_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "일치하는 회원 정보를 찾을 수 없습니다."),
    AUTH_INVALID_CREDENTIALS(1002, HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다."),
    AUTH_PASSWORD_MISMATCH(1003, HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    AUTH_DUPLICATED_USERNAME(1004, HttpStatus.BAD_REQUEST, "이미 존재하는 아이디입니다."),
    AUTH_DUPLICATED_EMAIL(1005, HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
    AUTH_INVALID_REFRESH_TOKEN(1006, HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    AUTH_PENDING_APPROVAL(1007, HttpStatus.UNAUTHORIZED, "계정이 승인되지 않았습니다."),
    AUTH_NOT_ACCESS_TOKEN(1008, HttpStatus.UNAUTHORIZED, "엑세스 토큰이 아닙니다."),
    AUTH_DELIVERY_MANAGER_NOT_FOUND(1009, HttpStatus.NOT_FOUND, "일치하는 배송 담당자 정보를 찾을 수 없습니다."),
    AUTH_HUB_ID_REQUIRED(1010, HttpStatus.BAD_REQUEST, "허브 아이디가 누락 되었습니다."),
    AUTH_VENDOR_ID_REQUIRED(1011, HttpStatus.BAD_REQUEST, "업체 아이디가 누락 되었습니다."),
    AUTH_DELIVERY_TYPE_REQUIRED(1012, HttpStatus.BAD_REQUEST, "배달 담당자 유형이 누락 되었습니다."),
    AUTH_USER_DATA_MISMATCH(1013, HttpStatus.BAD_REQUEST, "회원 정보가 일치하지 않습니다.")

    ;

    private final int code;
    private final HttpStatus status;
    private final String details;
}

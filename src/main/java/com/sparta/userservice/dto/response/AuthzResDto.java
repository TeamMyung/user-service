package com.sparta.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthzResDto {

    private boolean permit;
    private String reason;

    public static AuthzResDto permit() {
        return new AuthzResDto(true, null);
    }

    public static AuthzResDto deny(String reason) {
        return new AuthzResDto(false, reason);
    }
}

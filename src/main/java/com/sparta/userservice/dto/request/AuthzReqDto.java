package com.sparta.userservice.dto.request;

import com.sparta.userservice.global.security.authz.Action;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AuthzReqDto {

    private String resource;
    private Action action;
    private UUID targetHubId;
    private UUID targetVendorId;
    private Long targetUserId;

}

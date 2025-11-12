package com.sparta.userservice.dto.response;

import com.sparta.userservice.domain.User;
import com.sparta.userservice.domain.UserRole;
import com.sparta.userservice.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserResDto {

    private Long userId;
    private String username;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private String slackAccountId;
    private UUID hubId;
    private UUID vendorId;

    public static GetUserResDto from(User user) {
        return GetUserResDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .slackAccountId(user.getSlackAccountId())
                .hubId(user.getHubId())
                .vendorId(user.getVendorId())
                .build();

    }
}

package com.sparta.userservice.dto.response;

import com.sparta.userservice.domain.User;
import com.sparta.userservice.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class CreateUserResDto {

    private String username;
    private String name;
    private String email;
    private UserRole role;
    private String slackAccountId;
    private UUID hubId;
    private UUID vendorId;
    private DeliveryManagerDto deliveryManagerDto;

    public static CreateUserResDto from(User user, DeliveryManagerDto deliveryManagerDto) {
        return CreateUserResDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .slackAccountId(user.getSlackAccountId())
                .hubId(user.getHubId())
                .vendorId(user.getVendorId())
                .deliveryManagerDto(deliveryManagerDto)
                .build();
    }
}

package com.sparta.userservice.dto.request;

import com.sparta.userservice.domain.DeliveryType;
import com.sparta.userservice.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateUserReqDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[0-9])[a-z0-9]{4,10}$",
            message = "아이디는 4-10자 이내의 소문자와 숫자 조합이어야 합니다."
    )
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,15}$",
            message = "비밀번호는 8-15 이내의 대소문자, 숫자, 특수문자(!@#$%^&*) 조합이어야 합니다."
    )
    private String password;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotNull
    private UserRole role;

    @NotBlank
    private String slackAccountId;

    private UUID hubId;
    private UUID vendorId;
    private DeliveryType deliveryType;
}

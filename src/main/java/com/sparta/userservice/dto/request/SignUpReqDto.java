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
public class SignUpReqDto {

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

    @NotBlank(message = "확인용 비밀번호를 입력해주세요.")
    private String confirmPassword;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email
    private String email;

    @NotBlank(message = "슬랙 계정을 입력해주세요.")
    private String slackAccountId;

    @NotNull(message = "유형을 입력해주세요.")
    private UserRole role;

    private UUID hubId;
    private UUID vendorId;
    private DeliveryType deliveryType;
}

package com.sparta.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FindIdReqDto {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;
}

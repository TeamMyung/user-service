package com.sparta.userservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class RejectUsersReqDto {

    @NotEmpty(message = "userIds는 필수값입니다.")
    private List<Long> userIds;
}

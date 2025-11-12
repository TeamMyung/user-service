package com.sparta.userservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RejectUsersReqDto {

    @NotEmpty(message = "userIds는 필수값입니다.")
    private List<Long> userIds;
}

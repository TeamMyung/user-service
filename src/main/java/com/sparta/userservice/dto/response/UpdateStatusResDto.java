package com.sparta.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusResDto {

    private List<Long> completed;
    private List<Failure> failed;

    @Getter
    @AllArgsConstructor
    public static class Failure {
        private Long userId;
        private String reason;
    }
}

package com.sparta.userservice.controller;

import com.sparta.userservice.dto.request.CreateUserReqDto;
import com.sparta.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    // 생성 -> 관리자
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserReqDto requestDto) {
        return ResponseEntity.ok()userService.createUser(requestDto)
    }

    // 특정 회원 조회 -> 관리자

    // 본인 조회 -> 전체

    // 수정 -> 관리자

    // 삭제 -> 관리자

}

package com.sparta.userservice.controller;

import com.sparta.userservice.dto.request.CreateUserReqDto;
import com.sparta.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    /**
     * 회원 생성 (관리자)
     */
    @PostMapping
    public ResponseEntity<?> createUser(
            @Valid @RequestBody CreateUserReqDto requestDto, Authentication auth
    ) {
        return ResponseEntity.ok(userService.createUser(requestDto, auth));
    }

    /**
     * 회원 정보 조회 (관리자)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable(name = "userId") Long userId, Authentication auth) {
        return ResponseEntity.ok(userService.getUser(userId, auth));
    }

    /**
     * 회원 정보 조회 (본인)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUser(Authentication auth) {
        return ResponseEntity.ok(userService.getUser(auth));
    }

    // 수정 -> 관리자

    // 삭제 -> 관리자

}

package com.sparta.userservice.controller;

import com.sparta.userservice.dto.request.FindIdReqDto;
import com.sparta.userservice.dto.request.FindPwReqDto;
import com.sparta.userservice.dto.request.SignUpReqDto;
import com.sparta.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpReqDto requestDto) {
        authService.signUp(requestDto);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@Valid @RequestBody FindIdReqDto requestDto) {
        return ResponseEntity.ok(authService.findId(requestDto));
    }

    @PostMapping("/find-pw")
    public ResponseEntity<?> findPw(@Valid @RequestBody FindPwReqDto requestDto) {
        return ResponseEntity.ok(authService.findPw(requestDto));
    }
}

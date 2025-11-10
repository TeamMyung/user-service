package com.sparta.userservice.controller;

import com.sparta.userservice.dto.request.FindIdReqDto;
import com.sparta.userservice.dto.request.FindPwReqDto;
import com.sparta.userservice.dto.request.SignUpReqDto;
import com.sparta.userservice.global.security.jwt.user.UserDetailsImpl;
import com.sparta.userservice.service.AuthService;
import com.sparta.userservice.service.TokenRedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenRedisService tokenRedisService;

    private static final String BEARER = "Bearer ";

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

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(HttpServletRequest request, Authentication auth) {
        String bearerToken = request.getHeader(AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            String access = bearerToken.substring(BEARER.length());
            tokenRedisService.blackListAccess(access);
        }

        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            String userId = String.valueOf(userDetails.getUser().getUserId());
            tokenRedisService.deleteRefresh(userId);
        }

        return ResponseEntity.ok().build();
    }
}

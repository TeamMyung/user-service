package com.sparta.userservice.controller;

import com.sparta.userservice.dto.request.AuthzReqDto;
import com.sparta.userservice.dto.response.AuthzResDto;
import com.sparta.userservice.global.security.authz.AuthzPolicyService;
import com.sparta.userservice.global.security.authz.AuthzPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/internal/authz")
public class AuthzController {

    private final AuthzPolicyService policy;

    @PostMapping("/check")
    @PreAuthorize("hasAuthority('SCOPE_SERVICE') or isAuthenticated()")
    public ResponseEntity<?> check(@RequestBody AuthzReqDto requestDto, Authentication auth) {
        AuthzPrincipal principal = AuthzPrincipal.from(auth);
        boolean permit = policy.decide(principal, requestDto);
        return ResponseEntity.ok(permit
                ? AuthzResDto.permit()
                : AuthzResDto.deny("FORBIDDEN_BY_POLICY")
        );
    }


}

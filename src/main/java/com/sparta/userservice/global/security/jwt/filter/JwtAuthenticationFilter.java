package com.sparta.userservice.global.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.userservice.domain.User;
import com.sparta.userservice.dto.request.SignInReqDto;
import com.sparta.userservice.global.security.jwt.JwtProvider;
import com.sparta.userservice.global.security.jwt.user.UserDetailsImpl;
import com.sparta.userservice.service.TokenRedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;

    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtAuthenticationFilter(JwtProvider jwtProvider, TokenRedisService tokenRedisService) {
        this.jwtProvider = jwtProvider;
        this.tokenRedisService = tokenRedisService;
        setFilterProcessesUrl("/v1/auth/sign-in");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            SignInReqDto requestDto = new ObjectMapper().readValue(request.getInputStream(), SignInReqDto.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUsername(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // 로그인 성공
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, Authentication authResult
    ) throws IOException, ServletException {
        User user = ((UserDetailsImpl) authResult.getPrincipal()).getUser();

        String access = jwtProvider.createAccessToken(user);
        String refresh = jwtProvider.createRefreshToken();

        tokenRedisService.saveRefresh(refresh);

        response.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + access);
        response.addHeader(REFRESH_TOKEN_HEADER, refresh);
        response.addHeader("Access-Control-Expose-Headers", AUTHORIZATION_HEADER + ", " + REFRESH_TOKEN_HEADER);
    }

    // 로그인 실패
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }
}
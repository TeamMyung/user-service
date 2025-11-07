package com.sparta.userservice.global.security.jwt.filter;

import com.sparta.userservice.global.exception.AuthException;
import com.sparta.userservice.global.security.jwt.JwtProvider;
import com.sparta.userservice.global.security.jwt.user.UserDetailsImpl;
import com.sparta.userservice.global.security.jwt.user.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.sparta.userservice.global.response.ErrorCode.AUTH_NOT_ACCESS_TOKEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;

    private static final String AUTHORIZATION_HEADER = AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            Claims claims = jwtProvider.validateAndParse(token);

            if (!"access".equals(claims.get(JwtProvider.TOKEN_TYPE, String.class))) {
                log.error("엑세스 토큰이 아님");
                throw new AuthException(AUTH_NOT_ACCESS_TOKEN);
            }

            String username = claims.get("preferred_username", String.class);
            if (StringUtils.hasText(username)) {
                UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/") ||
                path.startsWith("/v1/auth/") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs");
    }

    // =========================== 유틸 메서드 ===========================

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}

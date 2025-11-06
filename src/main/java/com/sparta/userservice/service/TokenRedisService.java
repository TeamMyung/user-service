package com.sparta.userservice.service;

import com.sparta.userservice.global.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private final StringRedisTemplate redis;
    private final JwtProvider jwtProvider;

    private static final String KEY_REFRESH = "refresh:";
    private static final String KEY_BLACKLIST = "blacklist:";

    // ============================== 리프레시 토큰 관리 ==============================

    /**
     * 리프레시 토큰 저장
     */
    public void saveRefresh(String refresh) {
        Claims claims = jwtProvider.parseToken(refresh);
        String rid = claims.getId();
        long ttlSeconds = secondsUntilExpiration(claims.getExpiration());

        redis.opsForValue().set(KEY_REFRESH + rid, refresh, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 리프레시 토큰 조회
     */
    public Optional<String> getRefresh(String rid) {
        return Optional.ofNullable(redis.opsForValue().get(KEY_REFRESH + rid));
    }

    // ============================== 블랙리스트 ==============================

    /**
     * 엑세스 토큰 블랙리스트 등록
     */
    public void blackListAccess(String access) {
        Claims claims = jwtProvider.parseToken(access);
        String aid = claims.getId();
        long ttlSeconds = secondsUntilExpiration(claims.getExpiration());

        redis.opsForValue().set(KEY_BLACKLIST + aid, "1", Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 엑세스 토큰 블랙리스트 여부 확인
     */
    public boolean isBlacklisted(String access) {
        Claims claims = jwtProvider.parseToken(access);
        String aid = claims.getId();

        return redis.hasKey(KEY_BLACKLIST + aid);
    }

    // ============================== 유틸 메서드 및 내부 클래스 ==============================

    /**
     * 만료 시각까지 남은 시간 계산
     */
    private long secondsUntilExpiration(Date expiration) {
        return Math.max(1, Duration.between(Instant.now(), expiration.toInstant()).getSeconds());
    }
}

package com.sparta.userservice.global.security.jwt;

import com.sparta.userservice.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@Slf4j
@Component
public class JwtProvider {

    private final Duration ACCESS_TOKEN_VALIDITY_DURATION = Duration.ofMinutes(15);
    private final Duration REFRESH_TOKEN_VALIDITY_DURATION = Duration.ofDays(14);

    public static final String CLAIM_PREFERRED_USERNAME = "preferred_username";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_HUB_ID = "hub_id";
    public static final String CLAIM_VENDOR_ID = "vendor_id";
    public static final String TOKEN_TYPE = "token_type";

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.secret.key}")
    private String secretBase64;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(Base64.getDecoder()
                .decode(secretBase64)
        );
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        String ati = createRandomUuid();

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getUserId()))
                .id(ati)
                .claim(CLAIM_PREFERRED_USERNAME, user.getUsername())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_HUB_ID, user.getHubId())
                .claim(CLAIM_VENDOR_ID, user.getVendorId())
                .claim(TOKEN_TYPE, "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ACCESS_TOKEN_VALIDITY_DURATION)))
                .signWith(secretKey, HS256)
                .compact();
    }

    public String createRefreshToken() {
        Instant now = Instant.now();
        String rti = createRandomUuid();

        return Jwts.builder()
                .issuer(issuer)
                .id(rti)
                .claim(TOKEN_TYPE, "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(REFRESH_TOKEN_VALIDITY_DURATION)))
                .signWith(secretKey, HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .requireIssuer(issuer)
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않는 토큰입니다.: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰입니다.: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("잘못된 토큰입니다.: {}", e.getMessage());
        }
        return false;
    }

    private String createRandomUuid() {
        return UUID.randomUUID().toString();
    }
}

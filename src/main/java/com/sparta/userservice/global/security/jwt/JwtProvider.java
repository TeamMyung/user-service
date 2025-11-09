package com.sparta.userservice.global.security.jwt;

import com.sparta.userservice.domain.DeliveryManager;
import com.sparta.userservice.domain.User;
import com.sparta.userservice.global.PermissionMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final PermissionMapper permissionMapper;

    private final Duration ACCESS_TOKEN_VALIDITY_DURATION = Duration.ofMinutes(15);
    private final Duration REFRESH_TOKEN_VALIDITY_DURATION = Duration.ofDays(14);

    public static final String CLAIM_PREFERRED_USERNAME = "preferred_username";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_PERMS = "perms";
    public static final String CLAIM_HUB_ID = "hub_id";
    public static final String CLAIM_VENDOR_ID = "vendor_id";
    public static final String CLAIM_DELIVERY_TYPE = "delivery_type";
    public static final String CLAIM_DELIVERY_HUB_ID = "delivery_hub_id";
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

    public String createAccessToken(User user, @Nullable DeliveryManager deliveryManager) {
        Instant now = Instant.now();
        List<String> perms = permissionMapper.permsFor(user.getRole());

        JwtBuilder access = Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getUserId()))
                .claim(CLAIM_PREFERRED_USERNAME, user.getUsername())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_PERMS, perms)
                .claim(CLAIM_HUB_ID, user.getHubId())
                .claim(CLAIM_VENDOR_ID, user.getVendorId())
                .claim(TOKEN_TYPE, "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ACCESS_TOKEN_VALIDITY_DURATION)))
                .signWith(secretKey, HS256);

        if (deliveryManager != null) {
            access.claim(CLAIM_DELIVERY_TYPE, deliveryManager.getType().name());
            access.claim(CLAIM_DELIVERY_HUB_ID, deliveryManager.getHubId());
        }

        return access.compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim(TOKEN_TYPE, "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(REFRESH_TOKEN_VALIDITY_DURATION)))
                .signWith(secretKey, HS256)
                .compact();
    }

    public Claims validateAndParse(String token) {
        try {
            return Jwts.parser()
                    .requireIssuer(issuer)
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않는 토큰입니다.: {}", e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰입니다.: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("잘못된 토큰입니다.: {}", e.getMessage());
            throw e;
        }
    }
}

package com.sparta.userservice.global.security.authz;

import com.sparta.userservice.domain.DeliveryType;
import com.sparta.userservice.domain.User;
import com.sparta.userservice.domain.UserRole;
import com.sparta.userservice.global.security.jwt.user.UserDetailsImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class AuthzPrincipal {

    private final Long userId;
    private final UserRole role;
    private final UUID hubId;
    private final UUID vendorId;
    private final DeliveryType deliveryType;

    public static AuthzPrincipal from(Authentication auth) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        User user = principal.getUser();

        return AuthzPrincipal.builder()
                .userId(user.getUserId())
                .role(user.getRole())
                .hubId(user.getHubId())
                .vendorId(user.getVendorId())
                .deliveryType(principal.getDeliveryManager() != null ? principal.getDeliveryManager().getType() : null)
                .build();
    }
}
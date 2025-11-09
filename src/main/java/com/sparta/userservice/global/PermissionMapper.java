package com.sparta.userservice.global;

import com.sparta.userservice.domain.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 권한 범위
 * 포맷 = USER:<ACTION>:<SCOPE>
 * ACTION - CREATE, READ, UPDATE, DELETE
 * SCOPE - GLOBAL, HUB, VENDOR
 */
@Component
public class PermissionMapper {

    public List<String> permsFor(UserRole role) {
        return switch (role) {

            case MASTER -> List.of(
                    "USER:MANAGE:GLOBAL"
            );

            case HUB_MANAGER -> List.of(
                    "USER:READ:HUB",
                    "USER:READ:VENDOR",
                    "USER:CREATE:VENDOR",
                    "USER:UPDATE:VENDOR",
                    "USER:DELETE:VENDOR"
            );

            case VENDOR_MANAGER -> List.of(
                    "USER:READ:HUB",
                    "USER:READ:VENDOR",
                    "USER:UPDATE:VENDOR"
            );

            case DELIVERY_MANAGER -> List.of(
                    "USER:READ:HUB",
                    "USER:READ:VENDOR"
            );
        };
    }
}

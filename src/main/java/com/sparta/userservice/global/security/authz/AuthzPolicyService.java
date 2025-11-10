package com.sparta.userservice.global.security.authz;

import com.sparta.userservice.domain.UserRole;
import com.sparta.userservice.dto.request.AuthzReqDto;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

import static com.sparta.userservice.domain.UserRole.MASTER;
import static com.sparta.userservice.global.security.authz.Action.*;

@Service
public class AuthzPolicyService {

    public boolean decide(AuthzPrincipal principal, AuthzReqDto requestDto) {

        UserRole role = principal.getRole();

        // MASTER -> 전체 권한
        if (role == MASTER) return true;

        String resource = requestDto.getResource();
        Action action = requestDto.getAction();
        UUID targetHubId = requestDto.getTargetHubId();
        UUID targetVendorId = requestDto.getTargetVendorId();
        Long targetUserId = requestDto.getTargetUserId();

        return switch (resource) {
            case "HUB" -> hubPolicy(role, action);
            case "VENDOR" -> vendorPolicy(principal, role, action, targetHubId, targetVendorId);
            case "HUB_PATH" -> hubPathPolicy(role, action);
            case "DELIVERY_MANAGER" -> deliveryManagerPolicy(principal, role, action, targetHubId, targetUserId);
            case "PRODUCT" -> productPolicy(principal, role, action, targetHubId, targetVendorId);
            case "ORDER" -> orderPolicy(principal, role, action, targetHubId, targetUserId);
            case "DELIVERY" -> deliveryPolicy(principal, role, action, targetHubId, targetUserId);
            case "SLACK" -> slackPolicy(role, action);
            default -> false;
        };
    }

    /**
     * 허브
     * MASTER -> 전체 권한
     * HUB_MANAGER, VENDOR_MANAGER, DELIVERY_MANAGER -> 조회만 가능
     */
    private boolean hubPolicy(UserRole role, Action action) {
        return switch (role) {
            case HUB_MANAGER, VENDOR_MANAGER, DELIVERY_MANAGER -> actionEquals(action, READ);
            default -> false;
        };
    }

    /**
     * 허브 간 이동 정보
     * MASTER -> 전체 권한
     * HUB_MANAGER, VENDOR_MANAGER, DELIVERY_MANAGER -> READ
     */
    private boolean hubPathPolicy(UserRole role, Action action) {
        return switch (role) {
            case HUB_MANAGER, VENDOR_MANAGER, DELIVERY_MANAGER -> actionEquals(action, READ);
            default -> false;
        };
    }

    /**
     * 배송 담당자
     * MASTER -> 전체 권한
     * HUB_MANAGER -> 전체 권한 (소속 허브)
     * => HUB_TO_HUB의 경우 물류 센터 소속이기 때문에 관리 불가능
     * DELIVERY_MANAGER -> READ (본인 정보)
     */
    private boolean deliveryManagerPolicy(AuthzPrincipal principal, UserRole role, Action action, UUID targetHubId, Long targetUserId) {
        switch (role) {
            case HUB_MANAGER -> {
                if (!actionIn(action, CREATE, READ, UPDATE, DELETE)) return false;
                if (targetHubId == null) return false;
                return Objects.equals(principal.getHubId(), targetHubId);
            }
            case DELIVERY_MANAGER -> {
                return actionEquals(action, READ) && Objects.equals(principal.getUserId(), targetUserId);
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * 업체
     * MASTER -> 전체 권한
     * HUB_MANAGER -> 전체 권한 (하위 업체)
     * VENDOR_MANAGER -> READ (전체), UPDATE (소속 업체)
     * DELIVERY_MANAGER -> READ
     */
    private boolean vendorPolicy(AuthzPrincipal principal, UserRole role, Action action,
                                 UUID targetHubId, UUID targetVendorId) {
        switch (role) {
            case HUB_MANAGER -> {
                if (!actionIn(action, CREATE, READ, UPDATE, DELETE)) return false;
                return Objects.equals(principal.getHubId(), targetHubId);
            }
            case VENDOR_MANAGER -> {
                if (actionEquals(action, UPDATE)) {
                    return Objects.equals(principal.getVendorId(), targetVendorId);
                }
                return actionEquals(action, READ);
            }
            case DELIVERY_MANAGER -> {
                return actionEquals(action, READ);
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * 상품
     * MASTER -> 전체 권한
     * HUB_MANAGER -> 전체 권한 (소속 허브)
     * VENDOR_MANAGER -> READ (전체), CREATE, UPDATE (소속 업체)
     * DELIVERY_MANAGER -> READ
     */
    private boolean productPolicy(AuthzPrincipal principal, UserRole role, Action action,
                                  UUID targetHubId, UUID targetVendorId) {
        switch (role) {
            case HUB_MANAGER -> {
                if (!actionIn(action, CREATE, READ, UPDATE, DELETE)) return false;
                return Objects.equals(principal.getHubId(), targetHubId);
            }
            case VENDOR_MANAGER -> {
                if (actionEquals(action, CREATE) || actionEquals(action, UPDATE)) {
                    return Objects.equals(principal.getVendorId(), targetVendorId);
                }
                return actionEquals(action, READ);
            }
            case DELIVERY_MANAGER -> {
                return actionEquals(action, READ);
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * 주문
     * MASTER -> 전체 권한
     * HUB_MANAGER -> 전체 권한 (소속 허브)
     * VENDOR_MANAGER -> CREATE, READ (본인 주문)
     * DELIVERY_MANAGER -> CREATE, READ (본인 주문)
     */
    private boolean orderPolicy(AuthzPrincipal principal, UserRole role, Action action,
                                UUID targetHubId, Long targetUserId) {
        switch (role) {
            case HUB_MANAGER -> {
                if (actionEquals(action, CREATE)) return true;
                if (actionIn(action, READ, UPDATE, DELETE)) {
                    return Objects.equals(principal.getHubId(), targetHubId);
                }
                return false;
            }
            case VENDOR_MANAGER, DELIVERY_MANAGER -> {
                if (actionEquals(action, CREATE)) return true;
                if (actionEquals(action, READ)) {
                    return Objects.equals(principal.getUserId(), targetUserId);
                }
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * 배송
     * MASTER -> 전체 권한
     * HUB_MANAGER -> READ, UPDATE, DELETE (소속 허브)
     * VENDOR_MANAGER -> READ
     * DELIVERY_MANAGER -> READ, UPDATE (본인 배송)
     */
    private boolean deliveryPolicy(AuthzPrincipal principal, UserRole role, Action action,
                                   UUID targetHubId, Long targetUserId) {
        switch (role) {
            case HUB_MANAGER -> {
                if (actionIn(action, READ, UPDATE, DELETE)) {
                    return Objects.equals(principal.getHubId(), targetHubId);
                }
                return false;
            }
            case VENDOR_MANAGER -> {
                return actionEquals(action, READ);
            }
            case DELIVERY_MANAGER -> {
                if (actionIn(action, UPDATE)) {
                    return Objects.equals(principal.getUserId(), targetUserId);
                }
                return actionEquals(action, READ);
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * 슬랙 메시지
     * MASTER -> 전체 권한
     * HUB_MANAGER, VENDOR_MANAGER, DELIVERY_MANAGER -> CREATE
     */
    private boolean slackPolicy(UserRole role, Action action) {
        if (CREATE == action) return role != null;
        return false;
    }

    // ========================= 유틸 메서드 =========================
    private boolean actionEquals(Action a, Action b) {
        return a == b;
    }

    private boolean actionIn(Action a, Action... ones) {
        for (Action o : ones) {
            if (o == a) return true;
        }
        return false;
    }
}

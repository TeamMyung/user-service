package com.sparta.userservice.global.security.role;

import com.sparta.userservice.domain.User;
import com.sparta.userservice.domain.UserRole;
import com.sparta.userservice.global.security.jwt.user.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component("policy")
public class UserRoleScope {

    // 허브
    public boolean canHub(String action, UUID hubId, Authentication auth) {
        Action a = Action.of(action);
        User me = principal(auth);
        if (me == null) return false;

        return switch (a) {
            case CREATE, UPDATE, DELETE -> isMaster(me);
            case READ -> isMaster(me) || isHubManager(me) || isVendorManager(me) || isDeliveryManager(me);
        };
    }

    // 허브 간 이동 경로
    public boolean canHubRoute(String action, UUID hubId, Authentication auth) {
        Action a = Action.of(action);
        User me = principal(auth);
        if (me == null) return false;

        return switch (a) {
            case CREATE, UPDATE, DELETE -> isMaster(me);
            case READ -> isMaster(me) || isHubManager(me) || isVendorManager(me) || isDeliveryManager(me);
        };
    }

    // 배송 담당자
    public boolean canDeliveryManager(String action, UUID targetHubId, Long targetUserId, Authentication auth) {
        Action a = Action.of(action);
        User me = principal(auth);
        if (me == null) return false;

        return switch (a) {
            case CREATE, UPDATE, DELETE ->
                    isMaster(me) || (isHubManager(me) && same(me.getHubId(), targetHubId)); // 허브관리자: 담당 허브만
            case READ -> isMaster(me)
                    || (isHubManager(me) && same(me.getHubId(), targetHubId)) // 허브 관리자: 담당 허브
                    || (isDeliveryManager(me) && isSelf(me, targetUserId));   // 배송 담당자: 본인 정보
        };
    }

    // 업체
    public boolean canVendor(String action, UUID hubId, Authentication auth) {
        Action a = Action.of(action);
        User me = principal(auth);
        if (me == null) return false;

        return switch (a) {
            case CREATE, UPDATE, DELETE -> {
                if (isMaster(me)) yield true;
                if (isHubManager(me)) yield same(me.getHubId(), hubId);
                if (isVendorManager(me)) yield a == Action.UPDATE;
                yield false;
            }
            case READ -> isMaster(me) || isHubManager(me) || isVendorManager(me) || isDeliveryManager(me);
        };
    }

    // 상품
    public boolean canProduct(String action, UUID hubId, UUID vendorId, Authentication auth) {
        Action a = Action.of(action);
        User me = principal(auth);
        if (me == null) return false;

        return switch (a) {
            case CREATE, UPDATE -> {
                if (isMaster(me)) yield true;
                if (isHubManager(me)) yield same(me.getHubId(), hubId);
                if (isVendorManager(me)) yield same(me.getVendorId(), vendorId);
                yield false;
            }
            case DELETE -> {
                if (isMaster(me)) yield true;
                if (isHubManager(me)) yield same(me.getHubId(), hubId);
                yield false;
            }
            case READ -> isMaster(me) || isHubManager(me) || isVendorManager(me) || isDeliveryManager(me);
        };
    }

    // 주문
    public boolean canOrder(String action, UUID hubId, UUID vendorId, Long ownerUserId, Authentication auth) {
        Action a = Action.of(action);
        User me = principal(auth);
        if (me == null) return false;

        return switch (a) {
            case CREATE -> isMaster(me) || isHubManager(me) || isVendorManager(me) || isDeliveryManager(me);
            case UPDATE, DELETE, READ -> {
                if (isMaster(me)) yield true;
                if (isHubManager(me)) yield same(me.getHubId(), hubId);
                if (isDeliveryManager(me) || isVendorManager(me)) yield isSelf(me, ownerUserId);
                yield false;
            }
        };
    }

    // 배송
    public boolean canDelivery(String action, UUID hubId, UUID vendorId, Long assigneeUserId, Authentication auth) {
        Action a = Action.of(action);
        User me = principal(auth);
        if (me == null) return false;

        return switch (a) {
            case CREATE -> isMaster(me);
            case UPDATE, DELETE, READ -> {
                if (isMaster(me)) yield true;
                if (isHubManager(me)) yield same(me.getHubId(), hubId);
                if (isDeliveryManager(me)) yield isSelf(me, assigneeUserId);
                if (isVendorManager(me)) yield a == Action.READ;
                yield false;
            }
        };
    }

    // 슬랙 메시지
    public boolean canSlack(String action, Authentication auth) {
        Action a = Action.of(action);
        User me = principal(auth);
        if (me == null) return false;

        return switch (a) {
            case CREATE -> isMaster(me) || isHubManager(me) || isDeliveryManager(me) || isVendorManager(me);
            case UPDATE, DELETE, READ -> isMaster(me);
        };
    }

    public boolean canReadUser(Long targetUserId, Authentication auth) {
        User me = principal(auth);
        return me != null && Objects.equals(me.getUserId(), targetUserId);
    }

    // ========================= 유틸 메서드 =========================

    private User principal(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        Object p = auth.getPrincipal();
        return (p instanceof UserDetailsImpl udi) ? udi.getUser() : null;
    }

    private boolean isMaster(User user) {
        return user.getRole() == UserRole.MASTER;
    }

    private boolean isHubManager(User user) {
        return user.getRole() == UserRole.HUB_MANAGER;
    }

    private boolean isVendorManager(User user) {
        return user.getRole() == UserRole.VENDOR_MANAGER;
    }

    private boolean isDeliveryManager(User user) {
        return user.getRole() == UserRole.DELIVERY_MANAGER;
    }

    private boolean same(UUID a, UUID b) {
        return a != null && a.equals(b);
    }

    private boolean isSelf(User me, Long ownerUserId) {
        return ownerUserId != null && ownerUserId.equals(me.getUserId());
    }
}
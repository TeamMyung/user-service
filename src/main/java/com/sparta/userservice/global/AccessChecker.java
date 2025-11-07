package com.sparta.userservice.global;

import com.sparta.userservice.domain.deliverymanager.DeliveryManager;
import com.sparta.userservice.domain.user.User;
import com.sparta.userservice.domain.user.UserRole;
import com.sparta.userservice.global.security.jwt.user.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.sparta.userservice.domain.deliverymanager.DeliveryType.HUB_TO_HUB;
import static com.sparta.userservice.domain.deliverymanager.DeliveryType.HUB_TO_VENDOR;
import static com.sparta.userservice.domain.user.UserRole.*;

@Component("authz")
public class AccessChecker {

    private boolean has(Authentication auth, UserRole role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    public boolean canManageHub(Authentication auth, UUID hubId) {
        if (has(auth, MASTER)) return true;

        User user = resolveUser(auth);
        UUID affiliatedHubId = user.getHubId();

        return has(auth, HUB_MANAGER)
                && affiliatedHubId != null
                && affiliatedHubId.equals(hubId);
    }

    public boolean canManageVendor(Authentication auth, UUID vendorId) {
        if (has(auth, MASTER)) return true;

        User user = resolveUser(auth);
        UUID affiliatedVendorId = user.getVendorId();

        return has(auth, VENDOR_MANAGER)
                && affiliatedVendorId != null
                && affiliatedVendorId.equals(vendorId);
    }

    public boolean canManageVendorUnderHub(Authentication auth, UUID hubId) {
        if (has(auth, MASTER)) return true;
        if (!has(auth, HUB_MANAGER)) return false;

        return canManageHub(auth, hubId);
    }

    public boolean canManageDelivery(Authentication auth, Long deliveryManagerId) {
        if (has(auth, MASTER)) return true;

        User user = resolveUser(auth);
        if (!user.getUserId().equals(deliveryManagerId)) return false;

        if (!user.isDeliveryManager()) return false;

        DeliveryManager deliveryManager = resolveDeliveryManager(auth);
        if (deliveryManager == null) return false;

        if (deliveryManager.getType() == HUB_TO_HUB) return true;

        if (deliveryManager.getType() == HUB_TO_VENDOR)
            return deliveryManager.getHubId() != null;

        return false;
    }

    // ============================= 유틸 메서드 =============================

    private User resolveUser(Authentication auth) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();

        return principal.getUser();
    }

    private DeliveryManager resolveDeliveryManager(Authentication auth) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();

        return principal.getDeliveryManager();
    }
}

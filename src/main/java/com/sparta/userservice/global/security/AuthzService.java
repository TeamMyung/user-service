package com.sparta.userservice.global.security;

import com.sparta.userservice.global.security.jwt.user.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("authz")
public class AuthzService {

    public boolean has(Authentication auth, String resource, String action, String scope) {
        String needed = resource + ":" + action + ":" + scope;
        String manageScope = resource + ":MANAGE:" + scope;
        String global = resource + ":" + action + ":GLOBAL";
        String manageGlobal = resource + ":MANAGE:GLOBAL";

        return auth.getAuthorities().stream().anyMatch(a -> {
            String s = a.getAuthority();
            return s.equals(needed) || s.equals(manageScope) || s.equals(global) || s.equals(manageGlobal);
        });
    }

    public boolean inSameHub(Authentication auth, UUID targetHubId) {
        if (targetHubId == null) return false;
        var me = (UserDetailsImpl) auth.getPrincipal();
        UUID myHub = me.getUser().getHubId();
        return myHub != null && myHub.equals(targetHubId);
    }

    public boolean inSameVendor(Authentication auth, UUID targetVendorId) {
        if (targetVendorId == null) return false;
        var me = (UserDetailsImpl) auth.getPrincipal();
        UUID myVendor = me.getUser().getVendorId();
        return myVendor != null && myVendor.equals(targetVendorId);
    }
}

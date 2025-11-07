package com.sparta.userservice.global.security.jwt.user;

import com.sparta.userservice.domain.deliverymanager.DeliveryManager;
import com.sparta.userservice.domain.user.User;
import com.sparta.userservice.global.exception.AuthException;
import com.sparta.userservice.repository.DeliveryManagerRepository;
import com.sparta.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.sparta.userservice.domain.user.UserRole.DELIVERY_MANAGER;
import static com.sparta.userservice.domain.user.UserStatus.APPROVE;
import static com.sparta.userservice.global.response.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final DeliveryManagerRepository deliveryManagerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(AUTH_USER_NOT_FOUND));

        if (!user.getStatus().equals(APPROVE)) {
            log.error("승인 대기중: status={}", user.getStatus());
            throw new AuthException(AUTH_PENDING_APPROVAL);
        }

        if (user.getRole().equals(DELIVERY_MANAGER)) {
            DeliveryManager deliveryManager = deliveryManagerRepository.findById(user.getUserId())
                    .orElseThrow(() -> new AuthException(AUTH_DELIVERY_MANAGER_NOT_FOUND));

            return new UserDetailsImpl(user, deliveryManager);
        }

        return new UserDetailsImpl(user);
    }
}

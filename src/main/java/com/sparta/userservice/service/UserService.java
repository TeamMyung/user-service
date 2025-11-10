package com.sparta.userservice.service;

import com.sparta.userservice.domain.DeliveryManager;
import com.sparta.userservice.domain.User;
import com.sparta.userservice.dto.request.CreateUserReqDto;
import com.sparta.userservice.dto.response.CreateUserResDto;
import com.sparta.userservice.dto.response.DeliveryManagerDto;
import com.sparta.userservice.dto.response.GetUserResDto;
import com.sparta.userservice.global.exception.AuthException;
import com.sparta.userservice.global.security.jwt.user.UserDetailsImpl;
import com.sparta.userservice.repository.DeliveryManagerRepository;
import com.sparta.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.sparta.userservice.domain.DeliveryType.HUB_TO_VENDOR;
import static com.sparta.userservice.domain.UserStatus.APPROVE;
import static com.sparta.userservice.domain.UserStatus.REJECT;
import static com.sparta.userservice.global.response.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final DeliveryManagerRepository deliveryManagerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @PreAuthorize("hasRole('MASTER')")
    public void approve(Long userId) {
        User user = findUserById(userId);
        user.updateStatus(APPROVE);
    }

    @Transactional
    @PreAuthorize("hasRole('MASTER')")
    public void reject(Long userId) {
        User user = findUserById(userId);
        user.updateStatus(REJECT);
    }

    @Transactional
    @PreAuthorize("hasRole('MASTER')")
    public CreateUserResDto createUser(CreateUserReqDto requestDto, Authentication auth) {
        // 1. 중복 확인
        String username = requestDto.getUsername().trim();
        String email = requestDto.getEmail().trim().toLowerCase();

        validateUniqueUsername(requestDto.getUsername().trim());
        validateUniqueEmail(requestDto.getEmail().trim());

        User user = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName())
                .email(email)
                .role(requestDto.getRole())
                .status(APPROVE)
                .slackAccountId(requestDto.getSlackAccountId())
                .build()
        );

        // 4. 권한 확인
        UUID hubId = requestDto.getHubId();
        switch (user.getRole()) {
            case HUB_MANAGER -> {
                requireHubId(requestDto);
                user.assignHubId(hubId);
            }
            case VENDOR_MANAGER -> {
                requireVendorId(requestDto);
                user.assignVendorId(requestDto.getVendorId());
            }
            case DELIVERY_MANAGER -> {
                requireDeliveryType(requestDto);
                user.assignAsDeliveryManager();

                if (requestDto.getDeliveryType() == HUB_TO_VENDOR) {
                    requireHubId(requestDto);
                    user.assignHubId(hubId);
                }
            }
        }

        DeliveryManagerDto deliveryManagerDto = null;
        if (user.getIsDeliveryManager()) {
            DeliveryManager deliveryManager = deliveryManagerRepository.save(
                    DeliveryManager.builder()
                            .user(user)
                            .type(requestDto.getDeliveryType())
                            .hubId(user.getHubId())
                            .build()
            );
            deliveryManagerDto = DeliveryManagerDto.from(deliveryManager);
        }

        return CreateUserResDto.from(user, deliveryManagerDto);
    }

    @PreAuthorize("@policy.canReadUser(#userId, authentication)")
    public GetUserResDto getUser(Long userId, Authentication auth) {
        User user = findUserById(userId);

        DeliveryManagerDto deliveryManagerDto = deliveryManagerRepository.findById(user.getUserId())
                .map(DeliveryManagerDto::from)
                .orElseGet(() -> {
                    log.warn("일치하는 배송 담당자 정보를 찾을 수 없음: userId={}", user.getUserId());
                    return DeliveryManagerDto.empty();
                });

        return GetUserResDto.from(user, deliveryManagerDto);
    }

    public GetUserResDto getUser(Authentication auth) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        User user = principal.getUser();
        DeliveryManagerDto deliveryManagerDto = DeliveryManagerDto.from(principal.getDeliveryManager());

        return GetUserResDto.from(user, deliveryManagerDto);
    }

    // ============================ 유틸 메서드 ============================

    private void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new AuthException(AUTH_DUPLICATED_USERNAME);
        }
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(AUTH_DUPLICATED_EMAIL);
        }
    }

    private void requireHubId(CreateUserReqDto requestDto) {
        if (requestDto.getHubId() == null) {
            log.error("허브 아이디 누락");
            throw new AuthException(AUTH_HUB_ID_REQUIRED);
        }
    }

    private void requireVendorId(CreateUserReqDto requestDto) {
        if (requestDto.getVendorId() == null) {
            log.error("업체 아이디 누락");
            throw new AuthException(AUTH_VENDOR_ID_REQUIRED);
        }
    }

    private void requireDeliveryType(CreateUserReqDto requestDto) {
        if (requestDto.getDeliveryType() == null) {
            log.error("배달 담당자 유형 누락");
            throw new AuthException(AUTH_DELIVERY_TYPE_REQUIRED);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("회원을 찾을 수 없음: userId={}", userId);
                    return new AuthException(AUTH_USER_NOT_FOUND);
                });
    }
}

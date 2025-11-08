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
    public CreateUserResDto createUser(CreateUserReqDto requestDto, Authentication auth) {
        // 1. 아이디 중복 확인
        String username = requestDto.getUsername();
        validateUniqueUsername(username);

        // 2. 이메일 중복 확인
        String email = requestDto.getEmail();
        validateUniqueEmail(email);

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
        DeliveryManagerDto deliveryManagerDto = null;
        UUID hubId = requestDto.getHubId();
        switch (user.getRole()) {
            case HUB_MANAGER -> {
                validateHubId(requestDto);
                user.assignHubId(hubId);
            }
            case VENDOR_MANAGER -> {
                validateVendorId(requestDto);
                user.assignVendorId(requestDto.getVendorId());
            }
            case DELIVERY_MANAGER -> {
                validateDeliveryType(requestDto);

                user.assignAsDeliveryManager();
                DeliveryManager deliveryManager = deliveryManagerRepository.save(
                        DeliveryManager.builder()
                                .user(user)
                                .type(requestDto.getDeliveryType())
                                .build()
                );

                if (deliveryManager.getType() == HUB_TO_VENDOR) {
                    validateHubId(requestDto);

                    user.assignHubId(hubId);
                    deliveryManager.assignHubId(hubId);
                }

                deliveryManagerDto = DeliveryManagerDto.from(deliveryManager);
            }
        }

        return CreateUserResDto.from(user, deliveryManagerDto);
    }

    @PreAuthorize("hasRole('MASTER')")
    public GetUserResDto getUser(Long userId, Authentication auth) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("회원을 찾을 수 없음: userId={}", userId);
                    return new AuthException(AUTH_USER_NOT_FOUND);
                });

        DeliveryManagerDto deliveryManagerDto = null;
        if (Boolean.TRUE.equals(user.getIsDeliveryManager())) {
            DeliveryManager deliveryManager = deliveryManagerRepository.findById(user.getUserId())
                    .orElseThrow(() -> {
                        log.error("배송 담당자를 찾을 수 없음: deliveryManagerId={}", user.getUserId());
                        return new AuthException(AUTH_DELIVERY_MANAGER_NOT_FOUND);
                    });
            deliveryManagerDto = DeliveryManagerDto.from(deliveryManager);
        }

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

    private void validateHubId(CreateUserReqDto requestDto) {
        if (requestDto.getHubId() == null) {
            log.error("허브 아이디 누락");
            throw new AuthException(AUTH_HUB_ID_REQUIRED);
        }
    }

    private void validateVendorId(CreateUserReqDto requestDto) {
        if (requestDto.getVendorId() == null) {
            log.error("업체 아이디 누락");
            throw new AuthException(AUTH_VENDOR_ID_REQUIRED);
        }
    }

    private void validateDeliveryType(CreateUserReqDto requestDto) {
        if (requestDto.getDeliveryType() == null) {
            log.error("배달 담당자 유형 누락");
            throw new AuthException(AUTH_DELIVERY_TYPE_REQUIRED);
        }
    }
}

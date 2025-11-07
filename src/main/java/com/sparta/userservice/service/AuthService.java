package com.sparta.userservice.service;

import com.sparta.userservice.domain.deliverymanager.DeliveryManager;
import com.sparta.userservice.domain.user.User;
import com.sparta.userservice.dto.request.SignUpReqDto;
import com.sparta.userservice.global.exception.AuthException;
import com.sparta.userservice.repository.DeliveryManagerRepository;
import com.sparta.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.sparta.userservice.domain.deliverymanager.DeliveryType.HUB_TO_VENDOR;
import static com.sparta.userservice.domain.user.UserStatus.PENDING;
import static com.sparta.userservice.global.response.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final DeliveryManagerRepository deliveryManagerRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 요청
     */
    @Transactional
    public void signUp(SignUpReqDto requestDto) {
        // 1. 아이디 중복 확인
        String username = requestDto.getUsername().trim();
        validateUniqueUsername(username);

        // 2. 이메일 중복 확인
        String email = requestDto.getEmail().trim().toLowerCase();
        validateUniqueEmail(email);

        // 3. 비밀번호 일치 여부 확인
        if (!requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
            log.error("비밀번호가 일치하지 않음");
            throw new AuthException(AUTH_PASSWORD_MISMATCH);
        }

        User user = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName())
                .email(email)
                .role(requestDto.getRole())
                .status(PENDING)
                .slackAccountId(requestDto.getSlackAccountId())
                .build()
        );

        // 4. 권한 확인
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
            }
        }
    }

// =============================== 유틸 메서드 ===============================

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

    private void validateHubId(SignUpReqDto requestDto) {
        if (requestDto.getHubId() == null) {
            log.error("허브 아이디 누락");
            throw new AuthException(AUTH_HUB_ID_REQUIRED);
        }
    }

    private void validateVendorId(SignUpReqDto requestDto) {
        if (requestDto.getVendorId() == null) {
            log.error("업체 아이디 누락");
            throw new AuthException(AUTH_VENDOR_ID_REQUIRED);
        }
    }

    private void validateDeliveryType(SignUpReqDto requestDto) {
        if (requestDto.getDeliveryType() == null) {
            log.error("배달 담당자 유형 누락");
            throw new AuthException(AUTH_DELIVERY_TYPE_REQUIRED);
        }
    }
}

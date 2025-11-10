package com.sparta.userservice.service;

import com.sparta.userservice.domain.DeliveryManager;
import com.sparta.userservice.domain.User;
import com.sparta.userservice.domain.UserStatus;
import com.sparta.userservice.dto.request.ApproveUsersReqDto;
import com.sparta.userservice.dto.request.CreateUserReqDto;
import com.sparta.userservice.dto.request.RejectUsersReqDto;
import com.sparta.userservice.dto.response.CreateUserResDto;
import com.sparta.userservice.dto.response.DeliveryManagerDto;
import com.sparta.userservice.dto.response.GetUserResDto;
import com.sparta.userservice.dto.response.UpdateStatusResDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * 가입 요청 일괄 승인
     */
    @Transactional
    @PreAuthorize("hasRole('MASTER')")
    public UpdateStatusResDto approveUsers(ApproveUsersReqDto requestDto, Authentication auth) {
        return updateStatus(requestDto.getUserIds(), auth, true);
    }

    /**
     * 가입 요청 일괄 거절
     */
    @Transactional
    @PreAuthorize("hasRole('MASTER')")
    public UpdateStatusResDto rejectUsers(RejectUsersReqDto requestDto, Authentication auth) {
        return updateStatus(requestDto.getUserIds(), auth, false);
    }

    /**
     * 관리자 - 회원 등록
     */
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

    /**
     * 관리자 - 단일 회원 정보 조회
     */
    @PreAuthorize("hasRole('MASTER')")
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

    /**
     * 회원 - 본인 정보 조회
     */
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

    private UpdateStatusResDto updateStatus(List<Long> userIds, Authentication auth, boolean approve) {
        // 1. 중복 제거
        List<Long> distinctUserIds = userIds.stream().distinct().toList();

        // 2. 회원 조회
        Map<Long, User> users = userRepository.findAllById(distinctUserIds).stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        List<Long> completed = new ArrayList<>();
        List<UpdateStatusResDto.Failure> failed = new ArrayList<>();

        for (Long userId : distinctUserIds) {
            User user = users.get(userId);
            if (user == null) {
                failed.add(new UpdateStatusResDto.Failure(userId, AUTH_USER_NOT_FOUND.getDetails()));
                continue;
            }

            if (approve && user.getStatus() == APPROVE) {
                completed.add(userId);
                continue;
            }
            if (!approve && user.getStatus() == UserStatus.REJECT) {
                completed.add(userId);
                continue;
            }

            // 3. 상태 변경
            if (approve) {
                user.approve();
            } else {
                user.reject();
            }
            completed.add(userId);
        }

        return new UpdateStatusResDto(completed, failed);
    }
}

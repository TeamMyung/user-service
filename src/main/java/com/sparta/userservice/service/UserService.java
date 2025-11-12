package com.sparta.userservice.service;

import com.sparta.userservice.domain.User;
import com.sparta.userservice.domain.UserStatus;
import com.sparta.userservice.dto.request.ApproveUsersReqDto;
import com.sparta.userservice.dto.request.CreateUserReqDto;
import com.sparta.userservice.dto.request.RejectUsersReqDto;
import com.sparta.userservice.dto.response.CreateUserResDto;
import com.sparta.userservice.dto.response.GetSlackAccountIdResDto;
import com.sparta.userservice.dto.response.GetUserResDto;
import com.sparta.userservice.dto.response.UpdateStatusResDto;
import com.sparta.userservice.global.exception.UserException;
import com.sparta.userservice.global.security.jwt.user.UserDetailsImpl;
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

import static com.sparta.userservice.domain.UserStatus.APPROVE;
import static com.sparta.userservice.global.response.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
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
        UUID vendorId = requestDto.getVendorId();
        switch (user.getRole()) {
            case HUB_MANAGER -> {
                requireAffiliationId(hubId);
                user.assignHubId(hubId);
            }
            case VENDOR_MANAGER -> {
                requireVendorId(vendorId);
                user.assignVendorId(vendorId);
            }
            case DELIVERY_MANAGER -> {
                requireAffiliationId(hubId);
                user.assignHubId(hubId);
                user.assignAsDeliveryManager();
            }
        }
        return CreateUserResDto.from(user);
    }

    /**
     * 관리자 - 단일 회원 정보 조회
     */
    @PreAuthorize("hasRole('MASTER')")
    public GetUserResDto getUser(Long userId, Authentication auth) {
        return GetUserResDto.from(findUserById(userId));
    }

    /**
     * 회원 - 본인 정보 조회
     */
    public GetUserResDto getUser(Authentication auth) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        return GetUserResDto.from(principal.getUser());
    }

    /**
     * 슬랙 아이디 조회 (요청 응답)
     */
    public GetSlackAccountIdResDto getSlackAccountId(Long userId) {
        User user = findUserById(userId);
        return new GetSlackAccountIdResDto(user.getSlackAccountId());
    }

    // ============================ 유틸 메서드 ============================

    private void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UserException(USER_DUPLICATED_USERNAME);
        }
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserException(USER_DUPLICATED_EMAIL);
        }
    }

    private void requireAffiliationId(UUID hubId) {
        if (hubId == null) {
            log.error("허브 아이디 누락");
            throw new UserException(USER_HUB_ID_REQUIRED);
        }
    }

    private void requireVendorId(UUID vendorId) {
        if (vendorId == null) {
            log.error("업체 아이디 누락");
            throw new UserException(USER_VENDOR_ID_REQUIRED);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("회원을 찾을 수 없음: userId={}", userId);
                    return new UserException(USER_NOT_FOUND);
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
                failed.add(new UpdateStatusResDto.Failure(userId, USER_NOT_FOUND.getDetails()));
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

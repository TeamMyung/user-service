package com.sparta.userservice.service;

import com.sparta.userservice.domain.User;
import com.sparta.userservice.dto.request.FindIdReqDto;
import com.sparta.userservice.dto.request.FindPwReqDto;
import com.sparta.userservice.dto.request.SignUpReqDto;
import com.sparta.userservice.dto.response.FindIdResDto;
import com.sparta.userservice.dto.response.FindPwResDto;
import com.sparta.userservice.global.exception.UserException;
import com.sparta.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.sparta.userservice.domain.UserStatus.PENDING;
import static com.sparta.userservice.global.response.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

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
            throw new UserException(USER_PASSWORD_MISMATCH);
        }

        // 영속성 컨텍스트 저장
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

        // 4. 권한 확인 (더티 체킹)
        UUID hubId = requestDto.getHubId();
        UUID vendorId = requestDto.getVendorId();
        switch (user.getRole()) {
            case HUB_MANAGER -> {
                validateHubId(requestDto);
                user.assignHubId(hubId);
            }
            case VENDOR_MANAGER -> {
                validateVendorId(requestDto);
                user.assignHubId(vendorId);
            }
            case DELIVERY_MANAGER -> {
                validateHubId(requestDto);
                user.assignHubId(hubId);
                user.assignAsDeliveryManager();
            }
        }
    }

    public FindIdResDto findId(FindIdReqDto requestDto) {
        String email = requestDto.getEmail().trim();
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> {
                    log.error("이메일이 일치하는 회원 없음: email={}", email);
                    return new UserException(USER_NOT_FOUND);
                });

        String name = requestDto.getName();
        if (!user.getName().equals(name)) {
            log.error("회원 정보가 일치하지 않음: name={}", name);
            throw new UserException(USER_DATA_MISMATCH);
        }

        return new FindIdResDto(user.getUsername());
    }

    @Transactional
    public FindPwResDto findPw(FindPwReqDto requestDto) {
        String username = requestDto.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("아이디가 일치하는 회원 없음: username={}", username);
                    throw new UserException(USER_NOT_FOUND);
                });

        String name = requestDto.getName();
        String email = requestDto.getEmail();
        if (!user.getName().equals(name) || !user.getEmail().equals(email)) {
            log.error("회원 정보가 일치하지 않음: name={}, email={}", name, user.getEmail());
            throw new UserException(USER_DATA_MISMATCH);
        }

        String tempPassword = generateTempPassword();
        user.updatePassword(passwordEncoder.encode(tempPassword));

        return new FindPwResDto(tempPassword);
    }

// =============================== 유틸 메서드 ===============================

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

    private void validateHubId(SignUpReqDto requestDto) {
        if (requestDto.getHubId() == null) {
            log.error("허브 아이디 누락");
            throw new UserException(USER_HUB_ID_REQUIRED);
        }
    }

    private void validateVendorId(SignUpReqDto requestDto) {
        if (requestDto.getVendorId() == null) {
            log.error("업체 아이디 누락");
            throw new UserException(USER_VENDOR_ID_REQUIRED);
        }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        java.security.SecureRandom random = new java.security.SecureRandom();

        int length = 8 + random.nextInt(8);
        StringBuilder pw = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            pw.append(chars.charAt(random.nextInt(chars.length())));
        }

        return pw.toString();
    }
}

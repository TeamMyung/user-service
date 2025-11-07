package com.sparta.userservice.service;

import com.sparta.userservice.domain.User;
import com.sparta.userservice.dto.request.SignUpReqDto;
import com.sparta.userservice.global.exception.UserException;
import com.sparta.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (userRepository.existsByUsername(username)) {
            throw new UserException(USER_DUPLICATED_USERNAME);
        }

        // 2. 이메일 중복 확인
        String email = requestDto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new UserException(USER_DUPLICATED_EMAIL);
        }

        // 3. 비밀번호 일치 여부 확인
        if (!requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
            log.error("비밀번호가 일치하지 않음");
            throw new UserException(USER_PASSWORD_MISMATCH);
        }

        // 4. 추후 권한 관련 로직 추가

        userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName())
                .email(email)
                .role(requestDto.getRole())
                .status(PENDING)
                .slackAccountId(requestDto.getSlackAccountId())
                .build()
        );
    }

// =============================== 유틸 메서드 ===============================

}

package com.sparta.userservice.global.config;

import com.sparta.userservice.domain.User;
import com.sparta.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.sparta.userservice.domain.UserRole.MASTER;
import static com.sparta.userservice.domain.UserStatus.APPROVE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedMasterRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findUserByEmail("admin@sparta.com").isEmpty()) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin1234"))
                    .name("관리자")
                    .email("admin@sparta.com")
                    .role(MASTER)
                    .status(APPROVE)
                    .slackAccountId("U1234567890")
                    .isDeliveryManager(false)
                    .build()
            );

            log.info("관리자 계정 생성 완료");
        }
    }
}

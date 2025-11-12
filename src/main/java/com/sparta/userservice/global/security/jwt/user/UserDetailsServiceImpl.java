package com.sparta.userservice.global.security.jwt.user;

import com.sparta.userservice.domain.User;
import com.sparta.userservice.global.exception.UserException;
import com.sparta.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.sparta.userservice.domain.UserStatus.APPROVE;
import static com.sparta.userservice.global.response.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        if (user.getStatus() != APPROVE) {
            log.error("승인 대기중: status={}", user.getStatus());
            throw new UserException(USER_PENDING_APPROVAL);
        }

        return new UserDetailsImpl(user);
    }
}

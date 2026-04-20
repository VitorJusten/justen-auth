package com.justen.auth.domain.service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import com.justen.auth.domain.repository.UserRepository;

import lombok.AllArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Service
@AllArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 30;

    public void loginSucceeded(String username) {

        repository.findByUsername(username).ifPresent(user -> {

            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(OffsetDateTime.now());
            user.setAccountLocked(false);
            user.setLockUntil(null);

            repository.save(user);
        });
    }

    public void loginFailed(String username) {

        repository.findByUsername(username).ifPresent(user -> {

            int attempts = user.getFailedLoginAttempts() + 1;

            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_ATTEMPTS) {

                user.setAccountLocked(true);
                user.setLockUntil(OffsetDateTime.now().plusMinutes(LOCK_MINUTES));
            }

            repository.save(user);
        });
    }

}
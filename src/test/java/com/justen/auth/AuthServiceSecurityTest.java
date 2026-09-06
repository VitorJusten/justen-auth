package com.justen.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.core.dto.AuthResponseDto;
import com.justen.auth.domain.exception.BusinessException;
import com.justen.auth.domain.model.OAuthClient;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.OAuthClientRepository;
import com.justen.auth.domain.repository.UserRepository;
import com.justen.auth.domain.service.AuthService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceSecurityTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthClientRepository oAuthClientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private OAuthClient client;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("securityuser");
        user.setPassword(passwordEncoder.encode("StrongPassword123!"));
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(OffsetDateTime.now());
        user = userRepository.save(user);

        client = new OAuthClient();
        client.setId(UUID.randomUUID());
        client.setClientId("app-client");
        client.setClientSecret(passwordEncoder.encode("app-secret"));
        client.setScopes("read,write");
        client.setAuthorizedGrantTypes("password,refresh_token,client_credentials");
        client.setActive(true);
        client = oAuthClientRepository.save(client);
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        AuthResponseDto response = authService.login("securityuser", "StrongPassword123!", "app-client", "app-secret");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void shouldRejectInvalidPasswordAndIncrementFailedAttempts() {
        assertThrows(BusinessException.class, () -> {
            authService.login("securityuser", "WrongPassword!", "app-client", "app-secret");
        });

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(1, updatedUser.getFailedLoginAttempts());
    }

    @Test
    void shouldLockAccountAfter5FailedAttempts() {
        for (int i = 0; i < 4; i++) {
            try {
                authService.login("securityuser", "WrongPassword!", "app-client", "app-secret");
            } catch (BusinessException ignored) {
            }
        }

        // 5ª tentativa: deve acionar o bloqueio
        assertThrows(BusinessException.class, () -> {
            authService.login("securityuser", "WrongPassword!", "app-client", "app-secret");
        });

        User lockedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(lockedUser.getAccountLocked());
        assertNotNull(lockedUser.getLockUntil());
    }

    @Test
    void shouldAuthenticateMachineToMachineClientCredentials() {
        AuthResponseDto response = authService.authenticateClientCredentials("app-client", "app-secret", "read", "127.0.0.1", "ServiceA");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("read", response.getScope());
        assertEquals("Bearer", response.getTokenType());
    }
}

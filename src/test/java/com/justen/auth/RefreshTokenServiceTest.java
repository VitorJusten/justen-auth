package com.justen.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.domain.exception.BusinessException;
import com.justen.auth.domain.model.OAuthClient;
import com.justen.auth.domain.model.RefreshToken;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.OAuthClientRepository;
import com.justen.auth.domain.repository.RefreshTokenRepository;
import com.justen.auth.domain.repository.UserRepository;
import com.justen.auth.domain.service.RefreshTokenService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthClientRepository oAuthClientRepository;

    private User testUser;
    private OAuthClient testClient;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("refreshtester");
        testUser.setPassword("pass123456");
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser = userRepository.save(testUser);

        testClient = new OAuthClient();
        testClient.setId(UUID.randomUUID());
        testClient.setClientId("frontend-client");
        testClient.setClientSecret("secret");
        testClient.setScopes("read,write");
        testClient.setAuthorizedGrantTypes("password,refresh_token");
        testClient.setActive(true);
        testClient = oAuthClientRepository.save(testClient);
    }

    @Test
    void shouldGenerateSecureHashedRefreshToken() {
        RefreshToken token = refreshTokenService.generate(testUser, testClient, null, "127.0.0.1", "JUnit");

        assertNotNull(token.getToken());
        assertNotNull(token.getTokenHash());
        assertNotNull(token.getFamilyId());
        assertEquals(false, token.getRevoked());
    }

    @Test
    void shouldRotateRefreshTokenSuccessfully() {
        RefreshToken initialToken = refreshTokenService.generate(testUser, testClient, null, "127.0.0.1", "JUnit");
        String initialPlainToken = initialToken.getToken();

        RefreshToken rotatedToken = refreshTokenService.rotate(initialPlainToken, "127.0.0.1", "JUnit");

        assertNotNull(rotatedToken.getToken());
        assertNotEquals(initialPlainToken, rotatedToken.getToken());
        assertEquals(initialToken.getFamilyId(), rotatedToken.getFamilyId());

        // Token antigo deve estar revogado
        RefreshToken oldInDb = refreshTokenRepository.findById(initialToken.getId()).orElseThrow();
        assertTrue(oldInDb.getRevoked());
    }

    @Test
    void shouldDetectReuseAndRevokeFamilyWhenRevokedTokenIsUsed() {
        RefreshToken initialToken = refreshTokenService.generate(testUser, testClient, null, "127.0.0.1", "JUnit");
        String initialPlainToken = initialToken.getToken();

        // 1ª rotação legítima
        RefreshToken rotatedToken = refreshTokenService.rotate(initialPlainToken, "127.0.0.1", "JUnit");

        // 2ª tentativa usando o token antigo já revogado (SIMULAÇÃO DE ROUBO/REUSO)
        assertThrows(BusinessException.class, () -> {
            refreshTokenService.rotate(initialPlainToken, "192.168.1.100", "Attacker");
        });

        // Toda a família deve ter sido revogada
        RefreshToken currentTokenInDb = refreshTokenRepository.findById(rotatedToken.getId()).orElseThrow();
        assertTrue(currentTokenInDb.getRevoked());
    }
}

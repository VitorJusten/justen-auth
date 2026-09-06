package com.justen.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.core.dto.MfaConfirmResponseDto;
import com.justen.auth.core.dto.MfaSetupResponseDto;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.UserRepository;
import com.justen.auth.domain.service.MfaService;
import com.justen.auth.domain.service.RecoveryCodeService;
import com.justen.auth.domain.service.TotpService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TotpMfaServiceTest {

    @Autowired
    private TotpService totpService;

    @Autowired
    private RecoveryCodeService recoveryCodeService;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("mfatester");
        user.setPassword("password123");
        user.setCreatedAt(OffsetDateTime.now());
        user = userRepository.save(user);
    }

    @Test
    void shouldGenerateValidSecretAndOtpAuthUri() {
        String secret = totpService.generateSecretKey();
        assertNotNull(secret);
        assertTrue(secret.length() >= 16);

        String uri = totpService.generateOtpAuthUri(secret, "tester");
        assertTrue(uri.startsWith("otpauth://totp/"));
        assertTrue(uri.contains(secret));
    }

    @Test
    void shouldInitiateTotpSetupSuccessfully() {
        MfaSetupResponseDto setup = mfaService.initiateTotpSetup(user.getId());
        assertNotNull(setup.getSecret());
        assertNotNull(setup.getOtpAuthUri());
        assertFalse(mfaService.isMfaEnabled(user.getId())); // ainda não confirmado
    }

    @Test
    void shouldGenerateAndConsumeSingleUseRecoveryCodes() {
        List<String> recoveryCodes = recoveryCodeService.generateAndSaveRecoveryCodes(user);
        assertNotNull(recoveryCodes);
        assertEquals(8, recoveryCodes.size());

        String firstCode = recoveryCodes.get(0);

        // 1º consumo: deve ter sucesso
        boolean consumed = recoveryCodeService.consumeRecoveryCode(user.getId(), firstCode);
        assertTrue(consumed);

        // 2º consumo do mesmo código: deve falhar (single-use)
        boolean consumedAgain = recoveryCodeService.consumeRecoveryCode(user.getId(), firstCode);
        assertFalse(consumedAgain);
    }
}

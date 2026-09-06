package com.justen.auth.domain.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.core.dto.MfaConfirmResponseDto;
import com.justen.auth.core.dto.MfaSetupResponseDto;
import com.justen.auth.core.enums.SecurityEventType;
import com.justen.auth.domain.exception.BusinessException;
import com.justen.auth.domain.model.MfaCredential;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.MfaCredentialRepository;
import com.justen.auth.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MfaService {

    private final MfaCredentialRepository mfaCredentialRepository;
    private final UserRepository userRepository;
    private final TotpService totpService;
    private final RecoveryCodeService recoveryCodeService;
    private final SecurityAuditService auditService;

    public boolean isMfaEnabled(UUID userId) {
        return mfaCredentialRepository.existsByUserIdAndEnabledTrue(userId);
    }

    @Transactional
    public MfaSetupResponseDto initiateTotpSetup(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        String secret = totpService.generateSecretKey();
        String otpAuthUri = totpService.generateOtpAuthUri(secret, user.getUsername());

        MfaCredential credential = mfaCredentialRepository.findByUserIdAndType(userId, "TOTP")
                .orElseGet(() -> MfaCredential.builder()
                        .user(user)
                        .type("TOTP")
                        .build());

        credential.setSecret(secret);
        credential.setEnabled(false);
        credential.setUpdatedAt(OffsetDateTime.now());

        mfaCredentialRepository.save(credential);

        return MfaSetupResponseDto.builder()
                .secret(secret)
                .otpAuthUri(otpAuthUri)
                .build();
    }

    @Transactional
    public MfaConfirmResponseDto confirmTotpSetup(UUID userId, String code) {
        MfaCredential credential = mfaCredentialRepository.findByUserIdAndType(userId, "TOTP")
                .orElseThrow(() -> new BusinessException("MFA setup was not initiated"));

        boolean isValid = totpService.verifyCode(credential.getSecret(), code);
        if (!isValid) {
            auditService.recordEvent(userId, SecurityEventType.MFA_FAILURE, null, null, "Failed TOTP setup confirmation");
            throw new BusinessException("Invalid MFA verification code");
        }

        credential.setEnabled(true);
        credential.setUpdatedAt(OffsetDateTime.now());
        mfaCredentialRepository.save(credential);

        List<String> recoveryCodes = recoveryCodeService.generateAndSaveRecoveryCodes(credential.getUser());

        auditService.recordEvent(userId, SecurityEventType.MFA_ENABLED, null, null, "MFA TOTP successfully enabled");

        return MfaConfirmResponseDto.builder()
                .enabled(true)
                .recoveryCodes(recoveryCodes)
                .message("MFA enabled successfully. Store your recovery codes securely.")
                .build();
    }

    @Transactional
    public boolean verifyMfa(UUID userId, String codeOrRecoveryCode) {
        Optional<MfaCredential> credOpt = mfaCredentialRepository.findByUserIdAndTypeAndEnabledTrue(userId, "TOTP");
        if (credOpt.isEmpty()) {
            return true;
        }

        MfaCredential credential = credOpt.get();

        if (codeOrRecoveryCode.length() == 6 && codeOrRecoveryCode.matches("\\d{6}")) {
            boolean valid = totpService.verifyCode(credential.getSecret(), codeOrRecoveryCode);
            if (valid) {
                auditService.recordEvent(userId, SecurityEventType.MFA_SUCCESS, null, null, "TOTP verified");
                return true;
            }
        }

        boolean recoveryValid = recoveryCodeService.consumeRecoveryCode(userId, codeOrRecoveryCode);
        if (recoveryValid) {
            auditService.recordEvent(userId, SecurityEventType.RECOVERY_CODE_USED, null, null, "Recovery code consumed");
            return true;
        }

        auditService.recordEvent(userId, SecurityEventType.MFA_FAILURE, null, null, "Invalid MFA code attempt");
        return false;
    }

    @Transactional
    public void disableMfa(UUID userId, String code) {
        MfaCredential credential = mfaCredentialRepository.findByUserIdAndTypeAndEnabledTrue(userId, "TOTP")
                .orElseThrow(() -> new BusinessException("MFA is not enabled for this account"));

        boolean isValid = verifyMfa(userId, code);
        if (!isValid) {
            throw new BusinessException("Invalid code. MFA could not be disabled.");
        }

        credential.setEnabled(false);
        mfaCredentialRepository.save(credential);
        recoveryCodeService.generateAndSaveRecoveryCodes(credential.getUser());

        auditService.recordEvent(userId, SecurityEventType.MFA_DISABLED, null, null, "MFA TOTP disabled by user");
    }
}

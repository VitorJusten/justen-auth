package com.justen.auth.domain.service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.core.utils.TokenHashUtils;
import com.justen.auth.domain.model.MfaRecoveryCode;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.MfaRecoveryCodeRepository;
import com.justen.infrastructure.AppProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecoveryCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final MfaRecoveryCodeRepository recoveryCodeRepository;
    private final AppProperties appProperties;

    @Transactional
    public List<String> generateAndSaveRecoveryCodes(User user) {
        recoveryCodeRepository.deleteByUserId(user.getId());

        int count = appProperties.getAuth().getMfa().getRecoveryCodesCount() != null
                ? appProperties.getAuth().getMfa().getRecoveryCodesCount()
                : 8;

        List<String> plainCodes = new ArrayList<>();
        List<MfaRecoveryCode> entities = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String code = generateSingleCode();
            plainCodes.add(code);

            String codeHash = TokenHashUtils.sha256Hex(normalizeCode(code));

            MfaRecoveryCode entity = MfaRecoveryCode.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .codeHash(codeHash)
                    .used(false)
                    .createdAt(OffsetDateTime.now())
                    .build();

            entities.add(entity);
        }

        recoveryCodeRepository.saveAll(entities);
        return plainCodes;
    }

    @Transactional
    public boolean consumeRecoveryCode(UUID userId, String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return false;
        }

        String normalized = normalizeCode(rawCode);
        String codeHash = TokenHashUtils.sha256Hex(normalized);

        Optional<MfaRecoveryCode> codeOpt = recoveryCodeRepository.findByUserIdAndCodeHashAndUsedFalse(userId, codeHash);
        if (codeOpt.isPresent()) {
            MfaRecoveryCode code = codeOpt.get();
            code.setUsed(true);
            code.setUsedAt(OffsetDateTime.now());
            recoveryCodeRepository.save(code);
            return true;
        }

        return false;
    }

    private String generateSingleCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                sb.append('-');
            }
            sb.append(CHARACTERS.charAt(SECURE_RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private String normalizeCode(String code) {
        return code.toUpperCase().replace("-", "").trim();
    }
}

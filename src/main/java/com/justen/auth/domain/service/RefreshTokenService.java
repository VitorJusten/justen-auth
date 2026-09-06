package com.justen.auth.domain.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.core.enums.SecurityEventType;
import com.justen.auth.core.utils.TokenHashUtils;
import com.justen.auth.domain.exception.BusinessException;
import com.justen.auth.domain.model.OAuthClient;
import com.justen.auth.domain.model.RefreshToken;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.RefreshTokenRepository;
import com.justen.infrastructure.AppProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de Refresh Token com Rotação (RTR), hashing seguro (SHA-256) e detecção de reuso por família
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityAuditService auditService;
    private final AppProperties appProperties;

    @Transactional
    public RefreshToken generate(User user, OAuthClient client) {
        return generate(user, client, null, null, null);
    }

    @Transactional
    public RefreshToken generate(User user, OAuthClient client, String familyId, String ipAddress, String userAgent) {
        String rawToken = TokenHashUtils.generateSecureToken();
        String tokenHash = TokenHashUtils.sha256Hex(rawToken);
        String finalFamilyId = familyId != null ? familyId : UUID.randomUUID().toString();

        long expirationSeconds = appProperties.getAuth().getRefreshExpiration() != null
                ? appProperties.getAuth().getRefreshExpiration()
                : 604800L;

        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUserId(user.getId());
        token.setClientId(client.getId());
        token.setToken(rawToken); // Mantido temporariamente no retorno ao cliente
        token.setTokenHash(tokenHash);
        token.setFamilyId(finalFamilyId);
        token.setIpAddress(ipAddress);
        token.setUserAgent(userAgent);
        token.setExpiration(OffsetDateTime.now().plusSeconds(expirationSeconds));
        token.setRevoked(false);

        RefreshToken saved = refreshTokenRepository.save(token);
        // Garante que o objeto retornado contém o token plano para o chamador entregar ao cliente
        saved.setToken(rawToken);
        return saved;
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
        auditService.recordEvent(userId, SecurityEventType.TOKEN_REVOKED, null, null, "All refresh tokens revoked for user");
    }

    @Transactional
    public void revokeFamily(String familyId, UUID userId) {
        if (familyId == null) return;
        List<RefreshToken> tokens = refreshTokenRepository.findByFamilyId(familyId);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
        auditService.recordEvent(userId, SecurityEventType.TOKEN_REVOKED, null, null, "Token family revoked: " + familyId);
    }

    @Transactional
    public RefreshToken rotate(String oldTokenString) {
        return rotate(oldTokenString, null, null);
    }

    @Transactional
    public RefreshToken rotate(String oldTokenString, String ipAddress, String userAgent) {
        String incomingHash = TokenHashUtils.sha256Hex(oldTokenString);

        // Busca preferencialmente por hash; fallback para token plano em migração
        RefreshToken oldToken = refreshTokenRepository.findByTokenHash(incomingHash)
                .or(() -> refreshTokenRepository.findByToken(oldTokenString))
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        UUID userId = oldToken.getUserId();

        // 1. DETECÇÃO DE REUSO DE REFRESH TOKEN (COMPROMETIMENTO DE CREDENCIAIS)
        if (Boolean.TRUE.equals(oldToken.getRevoked())) {
            log.warn("CRITICAL: Refresh token reuse detected for userId={}! Revoking token family {}", userId, oldToken.getFamilyId());
            revokeFamily(oldToken.getFamilyId(), userId);
            revokeAllUserTokens(userId);

            auditService.recordEvent(userId, SecurityEventType.TOKEN_REUSE_DETECTED, ipAddress, userAgent,
                    "Refresh token reuse detected. Family " + oldToken.getFamilyId() + " invalidated.");

            throw new BusinessException("Refresh token was already used or revoked. Token theft detected. All user sessions invalidated.");
        }

        // 2. VALIDAÇÃO DE EXPIRAÇÃO
        if (oldToken.getExpiration().isBefore(OffsetDateTime.now())) {
            oldToken.setRevoked(true);
            refreshTokenRepository.save(oldToken);
            throw new BusinessException("Refresh token expired");
        }

        // 3. ROTAÇÃO: Invalida o token anterior
        oldToken.setRevoked(true);

        // 4. GERA NOVO TOKEN NA MESMA FAMÍLIA
        String newRawToken = TokenHashUtils.generateSecureToken();
        String newHash = TokenHashUtils.sha256Hex(newRawToken);

        long expirationSeconds = appProperties.getAuth().getRefreshExpiration() != null
                ? appProperties.getAuth().getRefreshExpiration()
                : 604800L;

        RefreshToken newToken = new RefreshToken();
        newToken.setId(UUID.randomUUID());
        newToken.setUserId(userId);
        newToken.setClientId(oldToken.getClientId());
        newToken.setToken(newRawToken);
        newToken.setTokenHash(newHash);
        newToken.setFamilyId(oldToken.getFamilyId() != null ? oldToken.getFamilyId() : UUID.randomUUID().toString());
        newToken.setIpAddress(ipAddress);
        newToken.setUserAgent(userAgent);
        newToken.setExpiration(OffsetDateTime.now().plusSeconds(expirationSeconds));
        newToken.setRevoked(false);

        oldToken.setReplacedBy(newHash);
        refreshTokenRepository.save(oldToken);

        RefreshToken savedNewToken = refreshTokenRepository.save(newToken);
        savedNewToken.setToken(newRawToken);

        auditService.recordEvent(userId, SecurityEventType.TOKEN_REFRESH, ipAddress, userAgent,
                "Token rotated successfully in family " + newToken.getFamilyId());

        return savedNewToken;
    }
}

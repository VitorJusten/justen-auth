package com.justen.auth.domain.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.justen.auth.domain.model.OAuthClient;
import com.justen.auth.domain.model.RefreshToken;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.RefreshTokenRepository;
import com.justen.infrastructure.AppProperties;

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
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    public RefreshToken generate(User user, OAuthClient client) {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUserId(user.getId());
        token.setClientId(client.getId());
        token.setToken(UUID.randomUUID().toString());
        token.setExpiration(OffsetDateTime.now().plusSeconds(appProperties.getAuth().getRefreshExpiration()));
        token.setRevoked(false);
        return refreshTokenRepository.save(token);
    }

    public void revokeAllUserTokens(UUID userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    public RefreshToken rotate(String oldTokenString) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(oldTokenString)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (Boolean.TRUE.equals(oldToken.getRevoked())) {
            revokeAllUserTokens(oldToken.getUserId());
            throw new RuntimeException(
                    "Refresh token was revoked. Token theft detected. All user sessions invalidated.");
        }

        if (oldToken.getExpiration().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        oldToken.setRevoked(true);
        RefreshToken newToken = new RefreshToken();
        newToken.setId(UUID.randomUUID());
        newToken.setUserId(oldToken.getUserId());
        newToken.setClientId(oldToken.getClientId());
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setExpiration(OffsetDateTime.now().plusSeconds(appProperties.getAuth().getRefreshExpiration()));
        newToken.setRevoked(false);

        oldToken.setReplacedBy(newToken.getToken());
        refreshTokenRepository.save(oldToken);

        return refreshTokenRepository.save(newToken);
    }

}

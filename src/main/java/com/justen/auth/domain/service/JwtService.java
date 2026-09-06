package com.justen.auth.domain.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.justen.auth.domain.model.OAuthClient;
import com.justen.auth.domain.model.User;
import com.justen.infrastructure.AppProperties;

import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela emissão e validação de tokens JWT assimétricos (RS256) com kid dinâmico
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final KeyManagementService keyManagementService;
    private final AppProperties appProperties;

    public String generateAccessToken(User user, String clientId, List<String> scopes) {
        Instant now = Instant.now();
        long expiration = appProperties.getAuth().getExpiration() != null ? appProperties.getAuth().getExpiration() : 900L;

        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList())
                : List.of();

        JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(keyManagementService.getActiveRsaKey().getKeyID())
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appProperties.getAuth().getIssuer())
                .audience(List.of(appProperties.getAuth().getAudience()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiration))
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("roles", roles)
                .claim("client_id", clientId)
                .claim("scope", String.join(" ", scopes))
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    /**
     * Gera token para autenticação entre microsserviços (OAuth 2.0 Client Credentials)
     */
    public String generateClientCredentialsToken(OAuthClient client, List<String> scopes) {
        Instant now = Instant.now();
        long expiration = appProperties.getAuth().getExpiration() != null ? appProperties.getAuth().getExpiration() : 900L;

        JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(keyManagementService.getActiveRsaKey().getKeyID())
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appProperties.getAuth().getIssuer())
                .audience(List.of(appProperties.getAuth().getAudience()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiration))
                .subject(client.getClientId())
                .claim("client_id", client.getClientId())
                .claim("roles", List.of("ROLE_SERVICE"))
                .claim("scope", String.join(" ", scopes))
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    /**
     * Emite token temporário restrito de desafio MFA (válido por 5 minutos)
     */
    public String generateMfaChallengeToken(User user, String clientId) {
        Instant now = Instant.now();

        JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(keyManagementService.getActiveRsaKey().getKeyID())
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appProperties.getAuth().getIssuer())
                .audience(List.of(appProperties.getAuth().getAudience()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300)) // 5 minutos
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("client_id", clientId)
                .claim("scope", "mfa:challenge")
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    public org.springframework.security.oauth2.jwt.Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }
}

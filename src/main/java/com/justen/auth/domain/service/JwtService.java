package com.justen.auth.domain.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.justen.auth.domain.model.User;
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
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final AppProperties appProperties;

    public String generateAccessToken(User user, String clientId, List<String> scopes) {
        Instant now = Instant.now();

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toList());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appProperties.getAuth().getIssuer())
                .audience(List.of(appProperties.getAuth().getAudience()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(appProperties.getAuth().getExpiration()))
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("roles", roles)
                .claim("client_id", clientId)
                .claim("scope", scopes)
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

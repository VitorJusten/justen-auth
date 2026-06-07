package com.justen.auth.domain.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.justen.auth.core.dto.AuthResponseDto;
import com.justen.auth.domain.model.OAuthClient;
import com.justen.auth.domain.model.RefreshToken;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.OAuthClientRepository;
import com.justen.auth.domain.repository.UserRepository;
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
public class AuthService {

    private final UserRepository userRepository;
    private final OAuthClientRepository oAuthClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AppProperties appProperties;

    public AuthResponseDto login(String username, String password, String clientId, String clientSecret) {

        OAuthClient client = oAuthClientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Invalid client"));

        if (!passwordEncoder.matches(clientSecret, client.getClientSecret())) {
            throw new RuntimeException("Invalid client credentials");
        }

        if (!client.getActive()) {
            throw new RuntimeException("Client is inactive");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("User account is locked or disabled");
        }

        List<String> scopes = Arrays.asList(client.getScopes().split(","));
        String accessToken = jwtService.generateAccessToken(user, clientId, scopes);
        RefreshToken refreshToken = refreshTokenService.generate(user, client);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getAuth().getExpiration())
                .build();
    }

    public AuthResponseDto refresh(String refreshTokenValue) {
        RefreshToken newRefreshToken = refreshTokenService.rotate(refreshTokenValue);
        
        User user = userRepository.findById(newRefreshToken.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        OAuthClient client = oAuthClientRepository.findById(newRefreshToken.getClientId())
            .orElseThrow(() -> new RuntimeException("Client not found"));

        List<String> scopes = Arrays.asList(client.getScopes().split(","));
        String newAccessToken = jwtService.generateAccessToken(user, client.getClientId(), scopes);

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getAuth().getExpiration())
                .build();
    }
}

package com.justen.auth.domain.service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.core.enums.SecurityEventType;
import com.justen.auth.core.dto.AuthResponseDto;
import com.justen.auth.core.dto.ExternalUserInfo;
import com.justen.auth.domain.exception.BusinessException;
import com.justen.auth.domain.model.OAuthClient;
import com.justen.auth.domain.model.RefreshToken;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.OAuthClientRepository;
import com.justen.auth.domain.repository.UserRepository;
import com.justen.infrastructure.AppProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OAuthClientRepository oAuthClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final MfaService mfaService;
    private final ExternalIdentityService externalIdentityService;
    private final GoogleAuthService googleAuthService;
    private final SteamAuthService steamAuthService;
    private final SecurityAuditService auditService;
    private final AppProperties appProperties;

    @Transactional
    public AuthResponseDto login(String credential, String password, String clientId, String clientSecret) {
        return login(credential, password, clientId, clientSecret, null, null);
    }

    @Transactional
    public AuthResponseDto login(String credential, String password, String clientId, String clientSecret, String ipAddress, String userAgent) {
        OAuthClient client = validateClient(clientId, clientSecret);

        User user = userRepository.findByCredential(credential)
                .orElse(null);

        // Mitigação de timing attacks e proteção contra enumeração de usuários
        if (user == null) {
            passwordEncoder.matches(password, "$2a$12$dummyHashToPreventTimingEnumerationAttackDummyHashValue");
            auditService.recordEvent(null, SecurityEventType.LOGIN_FAILURE, ipAddress, userAgent, "User not found for credential: " + credential);
            throw new BusinessException("Invalid credentials");
        }

        // 1. Verificação de conta bloqueada
        if (!user.isAccountNonLocked()) {
            auditService.recordEvent(user.getId(), SecurityEventType.LOGIN_BLOCKED, ipAddress, userAgent, "Attempted login on locked account");
            throw new BusinessException("User account is locked. Please try again later.");
        }

        // 2. Validação da senha
        if (!passwordEncoder.matches(password, user.getPassword())) {
            int attempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= 5) {
                user.setAccountLocked(true);
                user.setLockUntil(OffsetDateTime.now().plusMinutes(15));
                auditService.recordEvent(user.getId(), SecurityEventType.ACCOUNT_LOCKED, ipAddress, userAgent, "Account locked due to 5 consecutive failed attempts");
            } else {
                auditService.recordEvent(user.getId(), SecurityEventType.LOGIN_FAILURE, ipAddress, userAgent, "Password mismatch. Failed attempts: " + attempts);
            }

            userRepository.save(user);
            throw new BusinessException("Invalid credentials");
        }

        // 3. Sucesso na validação de credencial primária
        user.setFailedLoginAttempts(0);
        user.setLockUntil(null);
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        // 4. Verificação de MFA
        if (mfaService.isMfaEnabled(user.getId())) {
            String mfaChallengeToken = jwtService.generateMfaChallengeToken(user, clientId);
            auditService.recordEvent(user.getId(), SecurityEventType.MFA_CHALLENGE_ISSUED, ipAddress, userAgent, "MFA challenge token issued");

            return AuthResponseDto.builder()
                    .mfaRequired(true)
                    .mfaToken(mfaChallengeToken)
                    .tokenType("Bearer")
                    .expiresIn(300)
                    .build();
        }

        // 5. Emissão dos tokens definitivos
        List<String> scopes = Arrays.asList(client.getScopes().split(","));
        String accessToken = jwtService.generateAccessToken(user, clientId, scopes);
        RefreshToken refreshToken = refreshTokenService.generate(user, client, null, ipAddress, userAgent);

        auditService.recordEvent(user.getId(), SecurityEventType.LOGIN_SUCCESS, ipAddress, userAgent, "Local login successful");

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getAuth().getExpiration())
                .build();
    }

    @Transactional
    public AuthResponseDto loginWithGoogle(String idToken, String clientId, String clientSecret, String ipAddress, String userAgent) {
        OAuthClient client = validateClient(clientId, clientSecret);
        ExternalUserInfo userInfo = googleAuthService.verifyIdToken(idToken);
        User user = externalIdentityService.resolveUser(userInfo, ipAddress, userAgent);

        if (mfaService.isMfaEnabled(user.getId())) {
            String mfaChallengeToken = jwtService.generateMfaChallengeToken(user, clientId);
            auditService.recordEvent(user.getId(), SecurityEventType.MFA_CHALLENGE_ISSUED, ipAddress, userAgent, "MFA challenge issued for Google login");

            return AuthResponseDto.builder()
                    .mfaRequired(true)
                    .mfaToken(mfaChallengeToken)
                    .tokenType("Bearer")
                    .expiresIn(300)
                    .build();
        }

        List<String> scopes = Arrays.asList(client.getScopes().split(","));
        String accessToken = jwtService.generateAccessToken(user, clientId, scopes);
        RefreshToken refreshToken = refreshTokenService.generate(user, client, null, ipAddress, userAgent);

        auditService.recordEvent(user.getId(), SecurityEventType.LOGIN_SUCCESS, ipAddress, userAgent, "Google federated login successful");

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getAuth().getExpiration())
                .build();
    }

    @Transactional
    public AuthResponseDto loginWithSteam(Map<String, String> openIdParams, String clientId, String clientSecret, String ipAddress, String userAgent) {
        OAuthClient client = validateClient(clientId, clientSecret);
        ExternalUserInfo userInfo = steamAuthService.verifySteamLogin(openIdParams);
        User user = externalIdentityService.resolveUser(userInfo, ipAddress, userAgent);

        if (mfaService.isMfaEnabled(user.getId())) {
            String mfaChallengeToken = jwtService.generateMfaChallengeToken(user, clientId);
            auditService.recordEvent(user.getId(), SecurityEventType.MFA_CHALLENGE_ISSUED, ipAddress, userAgent, "MFA challenge issued for Steam login");

            return AuthResponseDto.builder()
                    .mfaRequired(true)
                    .mfaToken(mfaChallengeToken)
                    .tokenType("Bearer")
                    .expiresIn(300)
                    .build();
        }

        List<String> scopes = Arrays.asList(client.getScopes().split(","));
        String accessToken = jwtService.generateAccessToken(user, clientId, scopes);
        RefreshToken refreshToken = refreshTokenService.generate(user, client, null, ipAddress, userAgent);

        auditService.recordEvent(user.getId(), SecurityEventType.LOGIN_SUCCESS, ipAddress, userAgent, "Steam federated login successful");

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getAuth().getExpiration())
                .build();
    }

    @Transactional
    public AuthResponseDto authenticateClientCredentials(String clientId, String clientSecret, String requestedScope, String ipAddress, String userAgent) {
        OAuthClient client = validateClient(clientId, clientSecret);

        if (!client.getAuthorizedGrantTypes().contains("client_credentials")) {
            throw new BusinessException("Client is not authorized for grant_type=client_credentials");
        }

        List<String> clientScopes = Arrays.asList(client.getScopes().split(","));
        List<String> grantedScopes = clientScopes;

        if (requestedScope != null && !requestedScope.isBlank()) {
            List<String> requestedList = Arrays.asList(requestedScope.split(" "));
            grantedScopes = requestedList.stream()
                    .filter(clientScopes::contains)
                    .toList();
            if (grantedScopes.isEmpty()) {
                throw new BusinessException("None of the requested scopes are authorized for this client");
            }
        }

        String accessToken = jwtService.generateClientCredentialsToken(client, grantedScopes);

        auditService.recordEvent(null, SecurityEventType.TOKEN_ISSUED, ipAddress, userAgent,
                "Issued client_credentials token for client: " + clientId);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(appProperties.getAuth().getExpiration())
                .scope(String.join(" ", grantedScopes))
                .build();
    }

    @Transactional
    public AuthResponseDto refresh(String refreshTokenValue) {
        return refresh(refreshTokenValue, null, null);
    }

    @Transactional
    public AuthResponseDto refresh(String refreshTokenValue, String ipAddress, String userAgent) {
        RefreshToken newRefreshToken = refreshTokenService.rotate(refreshTokenValue, ipAddress, userAgent);

        User user = userRepository.findById(newRefreshToken.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"));

        OAuthClient client = oAuthClientRepository.findById(newRefreshToken.getClientId())
                .orElseThrow(() -> new BusinessException("Client not found"));

        List<String> scopes = Arrays.asList(client.getScopes().split(","));
        String newAccessToken = jwtService.generateAccessToken(user, client.getClientId(), scopes);

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getAuth().getExpiration())
                .build();
    }

    private OAuthClient validateClient(String clientId, String clientSecret) {
        if (clientId == null || clientId.isBlank()) {
            throw new BusinessException("Client ID is required");
        }

        OAuthClient client = oAuthClientRepository.findByClientId(clientId)
                .orElseThrow(() -> new BusinessException("Invalid client"));

        if (clientSecret != null && !passwordEncoder.matches(clientSecret, client.getClientSecret()) && !clientSecret.equals(client.getClientSecret())) {
            throw new BusinessException("Invalid client credentials");
        }

        if (!Boolean.TRUE.equals(client.getActive())) {
            throw new BusinessException("Client is inactive");
        }

        return client;
    }
}

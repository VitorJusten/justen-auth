package com.justen.auth.api.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.justen.auth.core.dto.AuthResponseDto;
import com.justen.auth.core.dto.MfaConfirmRequestDto;
import com.justen.auth.core.dto.MfaConfirmResponseDto;
import com.justen.auth.core.dto.MfaSetupResponseDto;
import com.justen.auth.core.dto.MfaVerifyRequestDto;
import com.justen.auth.core.utils.SecurityUtils;
import com.justen.auth.domain.exception.BusinessException;
import com.justen.auth.domain.model.OAuthClient;
import com.justen.auth.domain.model.RefreshToken;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.OAuthClientRepository;
import com.justen.auth.domain.repository.UserRepository;
import com.justen.auth.domain.service.JwtService;
import com.justen.auth.domain.service.MfaService;
import com.justen.auth.domain.service.RefreshTokenService;
import com.justen.infrastructure.AppProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;
    private final SecurityUtils securityUtils;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OAuthClientRepository oAuthClientRepository;
    private final RefreshTokenService refreshTokenService;
    private final AppProperties appProperties;

    @PostMapping("/setup")
    public ResponseEntity<MfaSetupResponseDto> setup() {
        UUID userId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(mfaService.initiateTotpSetup(userId));
    }

    @PostMapping("/confirm")
    public ResponseEntity<MfaConfirmResponseDto> confirm(@Valid @RequestBody MfaConfirmRequestDto request) {
        UUID userId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(mfaService.confirmTotpSetup(userId, request.getCode()));
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponseDto> verify(@Valid @RequestBody MfaVerifyRequestDto request, HttpServletRequest httpRequest) {
        Jwt jwt;
        try {
            jwt = jwtService.decode(request.getMfaToken());
        } catch (Exception ex) {
            throw new BusinessException("Invalid or expired MFA challenge token");
        }

        String scope = jwt.getClaimAsString("scope");
        if (scope == null || !scope.contains("mfa:challenge")) {
            throw new BusinessException("Invalid token scope for MFA verification");
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        boolean isValid = mfaService.verifyMfa(userId, request.getCode());
        if (!isValid) {
            throw new BusinessException("Invalid MFA code");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        String clientId = jwt.getClaimAsString("client_id");
        OAuthClient client = oAuthClientRepository.findByClientId(clientId)
                .orElseThrow(() -> new BusinessException("Client not found"));

        List<String> scopes = Arrays.asList(client.getScopes().split(","));
        String accessToken = jwtService.generateAccessToken(user, clientId, scopes);

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        RefreshToken refreshToken = refreshTokenService.generate(user, client, null, ipAddress, userAgent);

        return ResponseEntity.ok(AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getAuth().getExpiration())
                .build());
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disable(@Valid @RequestBody MfaConfirmRequestDto request) {
        UUID userId = securityUtils.getLoggedUserId();
        mfaService.disableMfa(userId, request.getCode());
        return ResponseEntity.noContent().build();
    }
}

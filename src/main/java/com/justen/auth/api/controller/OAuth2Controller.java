package com.justen.auth.api.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.justen.auth.core.dto.AuthResponseDto;
import com.justen.auth.domain.exception.BusinessException;
import com.justen.auth.domain.service.AuthService;
import com.justen.auth.domain.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller OAuth 2.0 padrão para os endpoints /oauth2/token e /oauth2/revoke
 */
@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "client_id", required = false) String clientIdParam,
            @RequestParam(value = "client_secret", required = false) String clientSecretParam,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {

        String[] clientCredentials = extractClientCredentials(authHeader, clientIdParam, clientSecretParam);
        String clientId = clientCredentials[0];
        String clientSecret = clientCredentials[1];

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        Map<String, Object> response = new LinkedHashMap<>();

        if ("client_credentials".equalsIgnoreCase(grantType)) {
            AuthResponseDto authResponse = authService.authenticateClientCredentials(clientId, clientSecret, scope, ipAddress, userAgent);
            response.put("access_token", authResponse.getAccessToken());
            response.put("token_type", authResponse.getTokenType());
            response.put("expires_in", authResponse.getExpiresIn());
            if (authResponse.getScope() != null) {
                response.put("scope", authResponse.getScope());
            }
            return ResponseEntity.ok(response);
        }

        if ("refresh_token".equalsIgnoreCase(grantType)) {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new BusinessException("refresh_token parameter is required for grant_type=refresh_token");
            }
            AuthResponseDto authResponse = authService.refresh(refreshToken, ipAddress, userAgent);
            response.put("access_token", authResponse.getAccessToken());
            response.put("refresh_token", authResponse.getRefreshToken());
            response.put("token_type", authResponse.getTokenType());
            response.put("expires_in", authResponse.getExpiresIn());
            return ResponseEntity.ok(response);
        }

        if ("password".equalsIgnoreCase(grantType)) {
            if (username == null || password == null) {
                throw new BusinessException("username and password are required for grant_type=password");
            }
            AuthResponseDto authResponse = authService.login(username, password, clientId, clientSecret, ipAddress, userAgent);
            if (Boolean.TRUE.equals(authResponse.getMfaRequired())) {
                response.put("mfa_required", true);
                response.put("mfa_token", authResponse.getMfaToken());
                response.put("token_type", "Bearer");
                response.put("expires_in", authResponse.getExpiresIn());
                return ResponseEntity.ok(response);
            }
            response.put("access_token", authResponse.getAccessToken());
            response.put("refresh_token", authResponse.getRefreshToken());
            response.put("token_type", authResponse.getTokenType());
            response.put("expires_in", authResponse.getExpiresIn());
            return ResponseEntity.ok(response);
        }

        throw new BusinessException("Unsupported grant_type: " + grantType);
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint) {
        // Revogação de refresh token
        try {
            refreshTokenService.rotate(token); // se for reutilizado será marcado
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok().build();
    }

    private String[] extractClientCredentials(String authHeader, String paramId, String paramSecret) {
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic ")) {
            try {
                String base64Credentials = authHeader.substring(6).trim();
                byte[] decoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(decoded, StandardCharsets.UTF_8);
                String[] parts = credentials.split(":", 2);
                return new String[]{parts[0], parts.length > 1 ? parts[1] : null};
            } catch (Exception ignored) {
            }
        }
        return new String[]{paramId, paramSecret};
    }
}

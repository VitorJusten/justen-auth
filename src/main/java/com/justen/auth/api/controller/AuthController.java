package com.justen.auth.api.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.justen.auth.core.dto.AuthResponseDto;
import com.justen.auth.core.dto.input.UserAuthInputDto;
import com.justen.auth.domain.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller de autenticação tradicional e federada (Google, Steam)
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponseDto login(
            @Valid @RequestBody UserAuthInputDto req,
            @RequestHeader(value = "X-Client-Id", required = false, defaultValue = "justen-frontend-client") String clientId,
            @RequestHeader(value = "X-Client-Secret", required = false, defaultValue = "justen-secret") String clientSecret,
            HttpServletRequest request) {

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return authService.login(req.getUsername(), req.getPassword(), clientId, clientSecret, ipAddress, userAgent);
    }

    @PostMapping("/refresh")
    public AuthResponseDto refresh(
            @RequestHeader(value = "Refresh-Token", required = false) String refreshTokenHeader,
            @RequestParam(value = "refresh_token", required = false) String refreshTokenParam,
            HttpServletRequest request) {

        String token = refreshTokenHeader != null ? refreshTokenHeader : refreshTokenParam;
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return authService.refresh(token, ipAddress, userAgent);
    }

    @PostMapping("/google")
    public AuthResponseDto loginWithGoogle(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-Client-Id", required = false, defaultValue = "justen-frontend-client") String clientId,
            @RequestHeader(value = "X-Client-Secret", required = false, defaultValue = "justen-secret") String clientSecret,
            HttpServletRequest request) {

        String idToken = payload.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            idToken = payload.get("credential");
        }

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return authService.loginWithGoogle(idToken, clientId, clientSecret, ipAddress, userAgent);
    }

    @PostMapping("/steam")
    public AuthResponseDto loginWithSteam(
            @RequestBody Map<String, String> openIdParams,
            @RequestHeader(value = "X-Client-Id", required = false, defaultValue = "justen-frontend-client") String clientId,
            @RequestHeader(value = "X-Client-Secret", required = false, defaultValue = "justen-secret") String clientSecret,
            HttpServletRequest request) {

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return authService.loginWithSteam(openIdParams, clientId, clientSecret, ipAddress, userAgent);
    }
}

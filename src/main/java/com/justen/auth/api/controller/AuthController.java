package com.justen.auth.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.justen.auth.core.dto.AuthResponseDto;
import com.justen.auth.core.dto.input.UserAuthInputDto;
import com.justen.auth.domain.service.AuthService;

import lombok.AllArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponseDto login(
            @RequestBody UserAuthInputDto req,
            @RequestHeader(value = "X-Client-Id", required = false, defaultValue = "justen-frontend-client") String clientId,
            @RequestHeader(value = "X-Client-Secret", required = false, defaultValue = "justen-secret") String clientSecret) {

        return authService.login(req.getUsername(), req.getPassword(), clientId, clientSecret);
    }

    @PostMapping("/refresh")
    public AuthResponseDto refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        return authService.refresh(refreshToken);
    }
}

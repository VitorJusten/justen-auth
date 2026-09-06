package com.justen.auth.api.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Endpoint padrão OIDC UserInfo (/oauth2/userinfo)
 */
@RestController
@RequestMapping("/oauth2/userinfo")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        String sub = jwt.getSubject();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sub", sub);

        try {
            UUID userId = UUID.fromString(sub);
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                response.put("preferred_username", user.getUsername());
                response.put("roles", user.getRoles().stream().map(r -> r.getName()).toList());
                response.put("account_locked", user.getAccountLocked());
            }
        } catch (IllegalArgumentException ignored) {
            response.put("client_id", jwt.getClaimAsString("client_id"));
        }

        if (jwt.getClaim("scope") != null) {
            response.put("scope", jwt.getClaim("scope"));
        }

        return ResponseEntity.ok(response);
    }
}

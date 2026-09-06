package com.justen.auth.api.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.justen.auth.domain.service.KeyManagementService;

import lombok.RequiredArgsConstructor;

/**
 * Endpoint JWKS (RFC 7517) para expor as chaves públicas do servidor de autorização
 */
@RestController
@RequiredArgsConstructor
public class JwksController {

    private final KeyManagementService keyManagementService;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getJwkSet() {
        return keyManagementService.getJwkSet().toJSONObject();
    }
}

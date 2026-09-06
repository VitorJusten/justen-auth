package com.justen.auth.api.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.justen.infrastructure.AppProperties;

import lombok.RequiredArgsConstructor;

/**
 * Endpoint padrão OIDC Discovery (/.well-known/openid-configuration)
 */
@RestController
@RequiredArgsConstructor
public class OidcDiscoveryController {

    private final AppProperties appProperties;

    @GetMapping(value = "/.well-known/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getOpenIdConfiguration() {
        String issuer = appProperties.getAuth().getIssuer();
        if (!issuer.startsWith("http://") && !issuer.startsWith("https://")) {
            issuer = "http://localhost:" + (appProperties.getServer() != null && appProperties.getServer().getPort() != null ? appProperties.getServer().getPort() : "8081");
        }

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("issuer", issuer);
        config.put("authorization_endpoint", issuer + "/oauth2/authorize");
        config.put("token_endpoint", issuer + "/oauth2/token");
        config.put("userinfo_endpoint", issuer + "/oauth2/userinfo");
        config.put("jwks_uri", issuer + "/.well-known/jwks.json");
        config.put("revocation_endpoint", issuer + "/oauth2/revoke");
        config.put("response_types_supported", List.of("code"));
        config.put("subject_types_supported", List.of("public"));
        config.put("id_token_signing_alg_values_supported", List.of("RS256"));
        config.put("scopes_supported", List.of("openid", "profile", "email", "read", "write"));
        config.put("token_endpoint_auth_methods_supported", List.of("client_secret_basic", "client_secret_post", "none"));
        config.put("claims_supported", List.of("sub", "iss", "aud", "exp", "iat", "username", "roles", "client_id", "scope"));
        config.put("code_challenge_methods_supported", List.of("S256"));
        config.put("grant_types_supported", List.of("authorization_code", "client_credentials", "refresh_token"));

        return config;
    }
}

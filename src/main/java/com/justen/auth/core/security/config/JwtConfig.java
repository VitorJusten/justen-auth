package com.justen.auth.core.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.justen.auth.domain.service.KeyManagementService;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.RequiredArgsConstructor;

/**
 * Configuração de codificação e decodificação de JWT utilizando chaves persistentes com suporte a rotação
 */
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final KeyManagementService keyManagementService;

    @Bean
    JWKSource<SecurityContext> jwkSource() {
        return (JWKSelector jwkSelector, SecurityContext context) -> {
            JWKSet jwkSet = keyManagementService.getSigningJwkSet();
            return jwkSelector.select(jwkSet);
        };
    }

    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    JwtDecoder jwtDecoder() {
        try {
            // Utiliza a chave pública ativa para validação local
            return NimbusJwtDecoder.withPublicKey(keyManagementService.getActiveRsaKey().toRSAPublicKey()).build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to configure JwtDecoder with active RSA key", ex);
        }
    }
}

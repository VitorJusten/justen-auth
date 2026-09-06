package com.justen.auth.domain.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justen.auth.core.dto.ExternalUserInfo;
import com.justen.auth.domain.exception.BusinessException;
import com.justen.infrastructure.AppProperties;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExternalUserInfo verifyIdToken(String idTokenString) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(idTokenString);
            Map<String, Object> claims = signedJwt.getPayload().toJSONObject();

            String issuer = (String) claims.get("iss");
            if (issuer == null || (!issuer.equals("https://accounts.google.com") && !issuer.equals("accounts.google.com"))) {
                throw new BusinessException("Invalid Google token issuer: " + issuer);
            }

            String configuredClientId = appProperties.getAuth().getGoogle().getClientId();
            if (configuredClientId != null && !configuredClientId.isBlank()) {
                Object aud = claims.get("aud");
                boolean audMatch = false;
                if (aud instanceof String s && s.equals(configuredClientId)) {
                    audMatch = true;
                } else if (aud instanceof java.util.List<?> list && list.contains(configuredClientId)) {
                    audMatch = true;
                }
                if (!audMatch) {
                    throw new BusinessException("Google token audience does not match configured Client ID");
                }
            }

            String sub = (String) claims.get("sub");
            if (sub == null || sub.isBlank()) {
                throw new BusinessException("Google ID Token missing 'sub' claim");
            }

            String email = (String) claims.get("email");
            Boolean emailVerified = Boolean.TRUE.equals(claims.get("email_verified"));
            String name = (String) claims.get("name");

            return ExternalUserInfo.builder()
                    .provider("GOOGLE")
                    .providerUserId(sub)
                    .email(email)
                    .displayName(name)
                    .emailVerified(emailVerified != null && emailVerified)
                    .build();

        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("Failed to parse/validate Google ID Token", ex);
            throw new BusinessException("Invalid Google ID token");
        }
    }
}

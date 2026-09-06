package com.justen.auth.domain.service;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.domain.model.SigningKey;
import com.justen.auth.domain.repository.SigningKeyRepository;
import com.justen.infrastructure.AppProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeyManagementService {

    private final SigningKeyRepository signingKeyRepository;
    private final AppProperties appProperties;

    private final AtomicReference<RSAKey> currentActiveRsaKey = new AtomicReference<>();

    @PostConstruct
    public void init() {
        refreshActiveKey();
    }

    @Transactional
    public synchronized RSAKey refreshActiveKey() {
        OffsetDateTime now = OffsetDateTime.now();
        List<SigningKey> activeKeys = signingKeyRepository.findActiveSigningKeys(now);

        if (!activeKeys.isEmpty()) {
            SigningKey current = activeKeys.get(0);
            RSAKey rsaKey = toRsaKey(current);
            currentActiveRsaKey.set(rsaKey);
            log.info("Loaded active signing key with kid: {}", current.getKeyId());
            return rsaKey;
        }

        log.info("No active signing key found. Generating a new persistent RSA key pair...");
        return generateAndSaveNewKey();
    }

    public RSAKey getActiveRsaKey() {
        RSAKey key = currentActiveRsaKey.get();
        if (key == null) {
            return refreshActiveKey();
        }
        return key;
    }

    public JWKSet getSigningJwkSet() {
        RSAKey active = getActiveRsaKey();
        return new JWKSet(active);
    }

    @Transactional(readOnly = true)
    public JWKSet getPublicJwkSet() {
        OffsetDateTime now = OffsetDateTime.now();
        List<SigningKey> validKeys = signingKeyRepository.findAllValidKeys(now);

        List<com.nimbusds.jose.jwk.JWK> jwks = validKeys.stream()
                .map(this::toPublicRsaKey)
                .map(k -> (com.nimbusds.jose.jwk.JWK) k)
                .toList();

        if (jwks.isEmpty()) {
            RSAKey active = getActiveRsaKey();
            return new JWKSet(active.toPublicJWK());
        }

        return new JWKSet(jwks);
    }

    @Transactional(readOnly = true)
    public JWKSet getJwkSet() {
        return getPublicJwkSet();
    }

    @Transactional
    public synchronized RSAKey rotateKey() {
        log.info("Rotating signing key...");
        return generateAndSaveNewKey();
    }

    private RSAKey generateAndSaveNewKey() {
        try {
            int keySize = appProperties.getAuth().getKeys().getKeySize() != null ? appProperties.getAuth().getKeys().getKeySize() : 2048;
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            KeyPair keyPair = generator.generateKeyPair();

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            String kid = "key-" + UUID.randomUUID().toString().substring(0, 8);
            OffsetDateTime now = OffsetDateTime.now();
            long rotationDays = appProperties.getAuth().getKeys().getRotationDays() != null ? appProperties.getAuth().getKeys().getRotationDays() : 90L;
            long graceDays = appProperties.getAuth().getKeys().getGracePeriodDays() != null ? appProperties.getAuth().getKeys().getGracePeriodDays() : 7L;
            OffsetDateTime expiresAt = now.plusDays(rotationDays + graceDays);

            String pubPem = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String privPem = Base64.getEncoder().encodeToString(privateKey.getEncoded());

            SigningKey signingKey = SigningKey.builder()
                    .id(UUID.randomUUID())
                    .keyId(kid)
                    .algorithm("RS256")
                    .publicKey(pubPem)
                    .privateKey(privPem)
                    .active(true)
                    .createdAt(now)
                    .expiresAt(expiresAt)
                    .build();

            signingKeyRepository.save(signingKey);

            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(kid)
                    .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                    .build();

            currentActiveRsaKey.set(rsaKey);
            log.info("Successfully generated and persisted new signing key: kid={}, expiresAt={}", kid, expiresAt);
            return rsaKey;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate and persist RSA key pair", e);
        }
    }

    private RSAKey toRsaKey(SigningKey entity) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] pubBytes = Base64.getDecoder().decode(entity.getPublicKey());
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(pubBytes));

            byte[] privBytes = Base64.getDecoder().decode(entity.getPrivateKey());
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privBytes));

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(entity.getKeyId())
                    .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reconstruct RSAKey from stored credentials for kid: " + entity.getKeyId(), e);
        }
    }

    private RSAKey toPublicRsaKey(SigningKey entity) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] pubBytes = Base64.getDecoder().decode(entity.getPublicKey());
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(pubBytes));

            return new RSAKey.Builder(publicKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(entity.getKeyId())
                    .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reconstruct public RSAKey for kid: " + entity.getKeyId(), e);
        }
    }
}

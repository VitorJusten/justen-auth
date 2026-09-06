package com.justen.auth.domain.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.core.dto.ExternalUserInfo;
import com.justen.auth.core.enums.CredentialTypeEnum;
import com.justen.auth.core.enums.SecurityEventType;
import com.justen.auth.core.utils.TokenHashUtils;
import com.justen.auth.domain.model.ExternalIdentity;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.model.UserCredential;
import com.justen.auth.domain.repository.ExternalIdentityRepository;
import com.justen.auth.domain.repository.UserCredentialRepository;
import com.justen.auth.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalIdentityService {

    private final ExternalIdentityRepository externalIdentityRepository;
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditService auditService;

    @Transactional
    public User resolveUser(ExternalUserInfo userInfo, String ipAddress, String userAgent) {
        String provider = userInfo.getProvider().toUpperCase();
        String providerUserId = userInfo.getProviderUserId();

        // 1. Verifica se a identidade externa já está vinculada
        Optional<ExternalIdentity> existingIdentity = externalIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId);
        if (existingIdentity.isPresent()) {
            return existingIdentity.get().getUser();
        }

        // 2. Se email foi verificado, busca se já existe usuário local com esse email para vincular com segurança
        User targetUser = null;
        if (userInfo.isEmailVerified() && userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) {
            Optional<UserCredential> emailCred = userCredentialRepository.findByCredential(userInfo.getEmail());
            if (emailCred.isPresent()) {
                targetUser = emailCred.get().getUser();
            } else {
                targetUser = userRepository.findByUsername(userInfo.getEmail()).orElse(null);
            }
        }

        // 3. Caso não exista usuário correspondente, cria novo usuário
        if (targetUser == null) {
            String baseUsername = userInfo.getEmail() != null ? userInfo.getEmail().split("@")[0] : provider.toLowerCase() + "_" + providerUserId;
            String username = baseUsername;
            int counter = 1;
            while (userRepository.findByUsername(username).isPresent()) {
                username = baseUsername + counter++;
            }

            String randomPassword = TokenHashUtils.generateSecureToken();

            targetUser = new User();
            targetUser.setId(UUID.randomUUID());
            targetUser.setUsername(username);
            targetUser.setPassword(passwordEncoder.encode(randomPassword));
            targetUser.setAccountLocked(false);
            targetUser.setCreatedAt(OffsetDateTime.now());
            targetUser = userRepository.save(targetUser);

            if (userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) {
                UserCredential cred = new UserCredential();
                cred.setId(UUID.randomUUID());
                cred.setUser(targetUser);
                cred.setCredential(userInfo.getEmail());
                cred.setCredentialType(CredentialTypeEnum.EMAIL);
                userCredentialRepository.save(cred);
            }
        }

        // 4. Cria e vincula a ExternalIdentity
        ExternalIdentity identity = ExternalIdentity.builder()
                .id(UUID.randomUUID())
                .user(targetUser)
                .provider(provider)
                .providerUserId(providerUserId)
                .email(userInfo.getEmail())
                .displayName(userInfo.getDisplayName())
                .createdAt(OffsetDateTime.now())
                .build();

        externalIdentityRepository.save(identity);

        auditService.recordEvent(targetUser.getId(), SecurityEventType.EXTERNAL_ACCOUNT_LINKED, ipAddress, userAgent,
                "Linked external provider: " + provider + " (id=" + providerUserId + ")");

        return targetUser;
    }
}

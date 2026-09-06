package com.justen.auth.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.justen.auth.domain.model.MfaCredential;

@Repository
public interface MfaCredentialRepository extends JpaRepository<MfaCredential, UUID> {

    Optional<MfaCredential> findByUserIdAndType(UUID userId, String type);

    Optional<MfaCredential> findByUserIdAndTypeAndEnabledTrue(UUID userId, String type);

    boolean existsByUserIdAndEnabledTrue(UUID userId);
}

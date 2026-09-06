package com.justen.auth.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.justen.auth.domain.model.MfaRecoveryCode;

@Repository
public interface MfaRecoveryCodeRepository extends JpaRepository<MfaRecoveryCode, UUID> {

    List<MfaRecoveryCode> findByUserIdAndUsedFalse(UUID userId);

    Optional<MfaRecoveryCode> findByUserIdAndCodeHashAndUsedFalse(UUID userId, String codeHash);

    void deleteByUserId(UUID userId);
}

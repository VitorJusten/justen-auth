package com.justen.auth.domain.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.justen.auth.domain.model.SigningKey;

@Repository
public interface SigningKeyRepository extends JpaRepository<SigningKey, UUID> {

    Optional<SigningKey> findByKeyId(String keyId);

    @Query("SELECT k FROM SigningKey k WHERE k.active = true AND k.expiresAt > :now ORDER BY k.createdAt DESC")
    List<SigningKey> findAllValidKeys(OffsetDateTime now);

    @Query("SELECT k FROM SigningKey k WHERE k.active = true AND k.expiresAt > :now ORDER BY k.createdAt DESC")
    List<SigningKey> findActiveSigningKeys(OffsetDateTime now);
}

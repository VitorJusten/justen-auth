package com.justen.auth.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.justen.auth.domain.model.ExternalIdentity;

@Repository
public interface ExternalIdentityRepository extends JpaRepository<ExternalIdentity, UUID> {

    Optional<ExternalIdentity> findByProviderAndProviderUserId(String provider, String providerUserId);

    List<ExternalIdentity> findByUserId(UUID userId);

    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
}

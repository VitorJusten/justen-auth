package com.justen.auth.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.justen.auth.domain.model.OAuthClient;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
public interface OAuthClientRepository extends JpaRepository<OAuthClient, UUID> {
    Optional<OAuthClient> findByClientId(String clientId);
}

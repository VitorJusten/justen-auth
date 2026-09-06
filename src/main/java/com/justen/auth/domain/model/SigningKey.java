package com.justen.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "signing_key")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SigningKey {

    @Id
    @Column(name = "sk_cd_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "sk_tx_kid", unique = true, nullable = false, length = 100)
    private String keyId;

    @Column(name = "sk_tx_algorithm", nullable = false, length = 20)
    private String algorithm;

    @Column(name = "sk_tx_public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "sk_tx_private_key", nullable = false, columnDefinition = "TEXT")
    private String privateKey;

    @Column(name = "sk_nm_active", nullable = false)
    private Boolean active;

    @Column(name = "sk_dt_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "sk_dt_expires_at", nullable = false)
    private OffsetDateTime expiresAt;
}

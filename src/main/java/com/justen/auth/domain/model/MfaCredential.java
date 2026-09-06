package com.justen.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "mfa_credential")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MfaCredential {

    @Id
    @Column(name = "mfa_cd_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usac_cd_id", nullable = false)
    private User user;

    @Column(name = "mfa_tx_type", nullable = false, length = 50)
    private String type; // TOTP, WEBAUTHN

    @Column(name = "mfa_tx_secret", nullable = false, columnDefinition = "TEXT")
    private String secret;

    @Column(name = "mfa_nm_enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "mfa_dt_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "mfa_dt_updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}

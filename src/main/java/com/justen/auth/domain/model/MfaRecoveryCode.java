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
@Table(name = "mfa_recovery_code")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MfaRecoveryCode {

    @Id
    @Column(name = "mfrc_cd_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usac_cd_id", nullable = false)
    private User user;

    @Column(name = "mfrc_tx_code_hash", nullable = false, length = 128)
    private String codeHash;

    @Column(name = "mfrc_nm_used", nullable = false)
    private Boolean used;

    @Column(name = "mfrc_dt_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "mfrc_dt_used_at")
    private OffsetDateTime usedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.used == null) {
            this.used = false;
        }
        this.createdAt = OffsetDateTime.now();
    }
}

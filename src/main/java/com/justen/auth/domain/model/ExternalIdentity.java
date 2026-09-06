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
@Table(name = "external_identity")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExternalIdentity {

    @Id
    @Column(name = "exid_cd_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usac_cd_id", nullable = false)
    private User user;

    @Column(name = "exid_tx_provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "exid_tx_provider_user_id", nullable = false, length = 150)
    private String providerUserId;

    @Column(name = "exid_tx_email", length = 255)
    private String email;

    @Column(name = "exid_tx_display_name", length = 255)
    private String displayName;

    @Column(name = "exid_dt_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "exid_dt_updated_at")
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

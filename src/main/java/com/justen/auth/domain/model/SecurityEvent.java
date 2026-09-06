package com.justen.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.justen.auth.core.enums.SecurityEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
@Table(name = "security_event")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SecurityEvent {

    @Id
    @Column(name = "sece_cd_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "usac_cd_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sece_tx_event_type", nullable = false, length = 80)
    private SecurityEventType eventType;

    @Column(name = "sece_tx_ip_address", length = 64)
    private String ipAddress;

    @Column(name = "sece_tx_user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "sece_tx_details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "sece_dt_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = OffsetDateTime.now();
    }
}

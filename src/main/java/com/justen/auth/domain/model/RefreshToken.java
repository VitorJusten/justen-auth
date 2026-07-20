package com.justen.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Data
@Entity
@Table(name = "refresh_token")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RefreshToken {
    @Id
    @Column(name = "rt_cd_id")
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "usac_cd_id", nullable = false)
    private UUID userId;

    @Column(name = "clnt_cd_id")
    private UUID clientId;

    @Column(name = "rt_tx_token", nullable = false)
    private String token;

    @Column(name = "rt_dt_expiration", nullable = false)
    private OffsetDateTime expiration;

    @Column(name = "rt_nm_revoked")
    private Boolean revoked = false;

    @Column(name = "rt_tx_replaced_by")
    private String replacedBy;
	
}

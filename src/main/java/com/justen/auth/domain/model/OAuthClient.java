package com.justen.auth.domain.model;

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
@Table(name = "oauth_client")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OAuthClient {
    @Id
    @Column(name = "clnt_cd_id")
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "clnt_tx_client_id", unique = true, nullable = false)
    private String clientId;

    @Column(name = "clnt_tx_client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "clnt_tx_scopes", nullable = false)
    private String scopes;

    @Column(name = "clnt_tx_authorized_grant_types", nullable = false)
    private String authorizedGrantTypes;

    @Column(name = "clnt_nm_active", nullable = false)
    private Boolean active = true;
}

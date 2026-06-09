package com.justen.auth.domain.model;

import java.util.UUID;

import com.justen.auth.core.enums.CredentialTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "user_credential")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserCredential {

	@Id
	@EqualsAndHashCode.Include
	@Column(name = "uscr_cd_id", nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usac_cd_id", nullable = false)
	private User user;

	@Column(name = "uscr_tx_credential", nullable = false, unique = true)
	private String credential;

	@Enumerated(EnumType.STRING)
	@Column(name = "uscr_tx_credential_type", nullable = false, length = 50)
	private CredentialTypeEnum credentialType;

}
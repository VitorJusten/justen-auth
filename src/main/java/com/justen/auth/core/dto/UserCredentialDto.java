package com.justen.auth.core.dto;

import java.util.UUID;

import com.justen.auth.core.enums.CredentialTypeEnum;
import com.justen.auth.domain.model.UserCredential;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Data
@NoArgsConstructor
public class UserCredentialDto {

	private UUID id;
	private String credential;
	private CredentialTypeEnum credentialType;

	public UserCredentialDto(UserCredential entity) {
		this.id = entity.getId();
		this.credential = entity.getCredential();
		this.credentialType = entity.getCredentialType();
	}

}

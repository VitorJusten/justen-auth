package com.justen.auth.core.dto.input;

import com.justen.auth.core.enums.CredentialTypeEnum;
import com.justen.auth.domain.model.UserCredential;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Data
public class UserCredentialInputDto {

	@NotBlank
	private String credential;
	@NotNull
	private CredentialTypeEnum credentialType;

	public UserCredential toEntity() {
		UserCredential userCredential = new UserCredential();

		userCredential.setCredential(credential);
		userCredential.setCredentialType(credentialType);

		return userCredential;
	}

}

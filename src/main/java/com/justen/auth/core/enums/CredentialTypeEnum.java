package com.justen.auth.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Getter
@AllArgsConstructor
public enum CredentialTypeEnum {
	EMAIL(".*"),
	PHONE(".*"),
	STEAMID("^[0-9]{17}$");

	private String regexFromat;
}

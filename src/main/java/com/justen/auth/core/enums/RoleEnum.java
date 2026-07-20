package com.justen.auth.core.enums;

import java.util.UUID;

import com.justen.auth.domain.model.Role;

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
public enum RoleEnum {

	DEV(UUID.fromString("0a5982c8-baa4-4e66-8614-01cf35ad4cfc"), "DEV"),
	ADM(UUID.fromString("0b5982c8-baa4-4e66-8614-01cf35ad4cfc"), "ADM"),
	AUTHOR(UUID.fromString("0c5982c8-baa4-4e66-8614-01cf35ad4cfc"), "AUTHOR"),
	COLLECTOR(UUID.fromString("0d5982c8-baa4-4e66-8614-01cf35ad4cfc"), "COLLECTOR");

	private UUID id;

	private String name;

	public static RoleEnum fromEntity(Role role) {

		for (RoleEnum roleEnum : RoleEnum.values()) {
			if (roleEnum.getId().equals(role.getId())) {
				return roleEnum;
			}
		}
		return null;
	}
}

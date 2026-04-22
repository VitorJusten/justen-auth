package com.justen.auth.domain.model.dto;

import java.util.UUID;

import com.justen.auth.domain.model.Role;

import jakarta.validation.constraints.NotBlank;
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
public class RoleDto {
	
	@NotBlank
	private UUID id;
	@NotBlank
	private String name;

	public RoleDto(Role role) {
		this.id = role.getId();
		this.name = role.getName();
	}
	
}

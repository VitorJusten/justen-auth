package com.justen.auth.core.dto.input;

import com.justen.auth.domain.model.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleInputDto {

	@NotBlank
	private String name;

	public Role toEntity() {
		Role role = new Role();
		role.setName(this.name);
		return role;
	}

}

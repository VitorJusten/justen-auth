package com.justen.auth.domain.model.dto.input;

import java.util.List;
import java.util.UUID;

import com.justen.auth.domain.model.Role;
import com.justen.auth.domain.model.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Data
public class UserInputDto {

	@NotBlank
	private String username;
	@NotBlank
	private String password;
	private List<UUID> roleIds;

	public User toEntity() {

		User user = new User();

		user.setUsername(username);
		user.setPassword(password);
		
		if (roleIds != null && !roleIds.isEmpty()) {
			roleIds.forEach(id -> {
				Role role = new Role();
				role.setId(id);
				user.getRoles().add(role);
			});
		}

		return user;
	}

}

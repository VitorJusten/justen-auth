package com.justen.auth.core.dto.input;

import java.util.List;

import com.justen.auth.core.enums.RoleEnum;
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
	private List<RoleEnum> roles;

	public User toEntity() {

		User user = new User();

		user.setUsername(username);
		user.setPassword(password);

		if (roles != null && !roles.isEmpty()) {
			roles.forEach(role -> {
				Role roleObj = new Role();
				roleObj.setId(role.getId());
				user.getRoles().add(roleObj);
			});
		}

		return user;
	}

}

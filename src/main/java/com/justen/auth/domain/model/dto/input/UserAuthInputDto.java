package com.justen.auth.domain.model.dto.input;

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
public class UserAuthInputDto {


	@NotBlank
	private String username;
	@NotBlank
	private String password;
	
	public User toEntity() {

		User user = new User();

		user.setUsername(username);
		user.setPassword(password);
		user.setRoles(null);
		
		return user;
	}
	
}

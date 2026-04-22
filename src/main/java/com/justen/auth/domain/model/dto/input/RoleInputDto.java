package com.justen.auth.domain.model.dto.input;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.justen.auth.domain.model.User;
import com.justen.auth.domain.model.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName piloteiro-amador
 * @Year 2026
 *
 */
@Data
public class RoleInputDto {

	@NotBlank
	private String name;
	private List<UUID> users;

	public Role toEntity() {
		Role role = new Role();
		Set<User> entityUsers = new HashSet<>();

		role.setName(this.name);

		if (this.users != null && !this.users.isEmpty()) {
			for (UUID user : this.users) {
				User u = new User();

				u.setId(user);
				entityUsers.add(u);
			}
		}

		role.setUsers(entityUsers);
		return role;
	}

}

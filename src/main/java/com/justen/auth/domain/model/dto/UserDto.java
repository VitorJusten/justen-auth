package com.justen.auth.domain.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.justen.auth.domain.model.User;

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
public class UserDto {

	private UUID id;
	private String username;
	private Set<RoleDto> roles;
	private Boolean accountLocked;
	private OffsetDateTime createdAt;
	private OffsetDateTime lastLoginAt;
	private OffsetDateTime lockUntil;
	private OffsetDateTime updatedAt;

	public UserDto(User entity) {
		this.id = entity.getId();
		this.username = entity.getUsername();
		this.roles = entity.getRoles() != null ? entity.getRoles().stream()
				.map(r -> new RoleDto(r))
				.collect(Collectors.toSet()) : null;
		this.accountLocked = entity.getAccountLocked();
		this.createdAt = entity.getCreatedAt();
		this.lastLoginAt = entity.getLastLoginAt();
		this.lockUntil = entity.getLockUntil();
		this.updatedAt = entity.getUpdatedAt();
	}
}

package com.justen.auth.domain.repository.custom;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.justen.auth.domain.model.dto.UserDto;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
public interface UserRepositoryCustom {

	/**
	 * 
	 * @param pageable
	 * @param filter
	 * @param id
	 * @param username
	 * @param role
	 * @param accountLocked
	 * @param createdAt
	 * @param lastLoginAt
	 * @param lockUntil
	 * @param updatedAt
	 * @return
	 */
	Page<UserDto> getAll(
			Pageable pageable,
			String filter,
			String id,
			String username,
			String role,
			Boolean accountLocked,
			String createdAt,
			String lastLoginAt,
			String lockUntil,
			String updatedAt);

}

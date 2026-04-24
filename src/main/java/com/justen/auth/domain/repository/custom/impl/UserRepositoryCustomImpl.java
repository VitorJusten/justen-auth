package com.justen.auth.domain.repository.custom.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justen.auth.core.utils.DateUtils;
import com.justen.auth.domain.model.dto.RoleDto;
import com.justen.auth.domain.model.dto.UserDto;
import com.justen.auth.domain.repository.custom.UserRepositoryCustom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Repository
@AllArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

	@PersistenceContext
	private final EntityManager entityManager;
	private final DateUtils dateUtils;

	@Override
	public Page<UserDto> getAll(Pageable pageable, String filter, String id, String username, String role,
			Boolean accountLocked, String createdAt, String lastLoginAt, String lockUntil, String updatedAt) {

		StringBuilder sql = new StringBuilder();

		sql.append("""
				    SELECT
				        u.usac_cd_id,
				        u.usac_tx_username,

				        json_agg(
				            json_build_object(
				                'id', r.role_cd_id,
				                'name', r.role_tx_name
				            )
				        ) AS roles,

				        u.usac_nm_account_locked,
				        u.usac_dt_created_at,
				        u.usac_dt_last_login_at,
				        u.usac_dt_lock_until,
				        u.usac_dt_updated_at

				    FROM user_account u
				    JOIN user_role ur ON ur.usac_cd_id = u.usac_cd_id
				    JOIN role r ON r.role_cd_id = ur.role_cd_id

				    WHERE 1=1
				""");

		if (filter != null && !filter.isBlank()) {
			sql.append("""
					    AND (
					        CAST(u.usac_cd_id AS TEXT) ILIKE :filter
					        OR u.usac_tx_username ILIKE :filter
					        OR r.role_tx_name ILIKE :filter
					        OR CAST(u.usac_nm_account_locked AS TEXT) ILIKE :filter
					        OR CAST(u.usac_dt_created_at AS TEXT) ILIKE :filter
					        OR CAST(u.usac_dt_last_login_at AS TEXT) ILIKE :filter
					        OR CAST(u.usac_dt_lock_until AS TEXT) ILIKE :filter
					        OR CAST(u.usac_dt_updated_at AS TEXT) ILIKE :filter
					    )
					""");
		}

		if (id != null && !id.isBlank()) {
			sql.append(" AND CAST(u.usac_cd_id AS TEXT) ILIKE :id ");
		}

		if (username != null && !username.isBlank()) {
			sql.append(" AND u.usac_tx_username ILIKE :username ");
		}

		if (role != null && !role.isBlank()) {
			sql.append(" AND r.role_tx_name ILIKE :role ");
		}

		if (accountLocked != null) {
			sql.append(" AND u.usac_nm_account_locked = :accountLocked ");
		}

		if (createdAt != null && !createdAt.isBlank()) {
			sql.append(" AND CAST(u.usac_dt_created_at AS TEXT) ILIKE :createdAt ");
		}

		if (lastLoginAt != null && !lastLoginAt.isBlank()) {
			sql.append(" AND CAST(u.usac_dt_last_login_at AS TEXT) ILIKE :lastLoginAt ");
		}

		if (lockUntil != null && !lockUntil.isBlank()) {
			sql.append(" AND CAST(u.usac_dt_lock_until AS TEXT) ILIKE :lockUntil ");
		}

		if (updatedAt != null && !updatedAt.isBlank()) {
			sql.append(" AND CAST(u.usac_dt_updated_at AS TEXT) ILIKE :updatedAt ");
		}

		sql.append("""
				    GROUP BY
				        u.usac_cd_id,
				        u.usac_tx_username,
				        u.usac_nm_account_locked,
				        u.usac_dt_created_at,
				        u.usac_dt_last_login_at,
				        u.usac_dt_lock_until,
				        u.usac_dt_updated_at

				    ORDER BY u.usac_dt_created_at DESC
				""");

		Query query = entityManager.createNativeQuery(sql.toString());

		if (filter != null && !filter.isBlank()) {
			query.setParameter("filter", "%" + filter + "%");
		}

		if (id != null && !id.isBlank()) {
			query.setParameter("id", "%" + id + "%");
		}

		if (username != null && !username.isBlank()) {
			query.setParameter("username", "%" + username + "%");
		}

		if (role != null && !role.isBlank()) {
			query.setParameter("role", "%" + role + "%");
		}

		if (accountLocked != null) {
			query.setParameter("accountLocked", accountLocked);
		}

		if (createdAt != null && !createdAt.isBlank()) {
			query.setParameter("createdAt", "%" + createdAt + "%");
		}

		if (lastLoginAt != null && !lastLoginAt.isBlank()) {
			query.setParameter("lastLoginAt", "%" + lastLoginAt + "%");
		}

		if (lockUntil != null && !lockUntil.isBlank()) {
			query.setParameter("lockUntil", "%" + lockUntil + "%");
		}

		if (updatedAt != null && !updatedAt.isBlank()) {
			query.setParameter("updatedAt", "%" + updatedAt + "%");
		}

		query.setFirstResult((int) pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());

		List<Object[]> rows = query.getResultList();

		List<UserDto> users = new ArrayList<>();

		ObjectMapper mapper = new ObjectMapper();

		for (Object[] row : rows) {

			UserDto dto = new UserDto();

			dto.setId((UUID) row[0]);
			dto.setUsername((String) row[1]);

			try {
				Set<RoleDto> roles = mapper.readValue(row[2].toString(), new TypeReference<Set<RoleDto>>() {
				});
				dto.setRoles(roles);
			} catch (Exception e) {
				dto.setRoles(Set.of());
			}

			dto.setAccountLocked((Boolean) row[3]);
			dto.setCreatedAt(dateUtils.toOffsetDateTime(row[4]));
			dto.setLastLoginAt(dateUtils.toOffsetDateTime(row[5]));
			dto.setLockUntil(dateUtils.toOffsetDateTime(row[6]));
			dto.setUpdatedAt(dateUtils.toOffsetDateTime(row[7]));

			users.add(dto);
		}

		long total = users.size();

		return new PageImpl<>(users, pageable, total);
	}

}

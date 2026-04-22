package com.justen.auth.domain.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.justen.auth.core.utils.SecurityUtils;
import com.justen.auth.domain.enums.RoleEnum;
import com.justen.auth.domain.model.Role;
import com.justen.auth.domain.repository.RoleRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Service
@AllArgsConstructor
public class RoleService {

	private final RoleRepository roleRepository;
	private final SecurityUtils securityUtils;

	public List<Role> getAllRoles() {
		securityUtils.validateRoles(List.of(RoleEnum.ADM.getName(), RoleEnum.DEV.getName()));
		return roleRepository.findAll();
	}

	public Role getRoleById(UUID id) {
		return roleRepository.findById(id).orElseThrow(() -> new RuntimeException("Role not found"));
	}

	@Transactional
	public Role createRole(Role role) {
		securityUtils.validateRoles(List.of(RoleEnum.ADM.getName(), RoleEnum.DEV.getName()));
		return roleRepository.save(role);
	}

	@Transactional
	public Role updateRole(UUID id, Role role) {
		securityUtils.validateRoles(List.of(RoleEnum.ADM.getName(), RoleEnum.DEV.getName()));
		Role existing = getRoleById(id);

		BeanUtils.copyProperties(role, existing, "id");

		return roleRepository.save(existing);
	}

	@Transactional
	public void deleteRole(UUID id) {
		securityUtils.validateRoles(List.of(RoleEnum.ADM.getName(), RoleEnum.DEV.getName()));
		roleRepository.deleteById(id);
	}

}

package com.justen.auth.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.justen.auth.domain.model.Role;
import com.justen.auth.domain.service.RoleService;

import lombok.AllArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/role")
public class RoleController {

	private final RoleService service;

	@GetMapping
	public List<Role> getAll() {
		return service.getAllRoles();
	}

	@GetMapping("/{id}")
	public Role getById(@PathVariable UUID id) {
		return service.getRoleById(id);
	}

	@PostMapping
	public Role create(@RequestBody Role role) {
		return service.createRole(role);
	}

	@PutMapping("/{id}")
	public Role update(@PathVariable UUID id, @RequestBody Role role) {
		return service.updateRole(id, role);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable UUID id) {
		service.deleteRole(id);
	}

}
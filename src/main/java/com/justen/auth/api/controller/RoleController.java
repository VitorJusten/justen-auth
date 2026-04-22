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

import com.justen.auth.domain.model.dto.RoleDto;
import com.justen.auth.domain.model.dto.input.RoleInputDto;
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
	public List<RoleDto> getAll() {
		return service.getAllRoles().stream().map(RoleDto::new).toList();
	}

	@GetMapping("/{id}")
	public RoleDto getById(@PathVariable UUID id) {
		return new RoleDto(service.getRoleById(id));
	}

	@PostMapping
	public RoleDto create(@RequestBody RoleInputDto role) {
		return new RoleDto(service.createRole(role.toEntity()));
	}

	@PutMapping("/{id}")
	public RoleDto update(@PathVariable UUID id, @RequestBody RoleInputDto role) {
		return new RoleDto(service.updateRole(id, role.toEntity()));
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable UUID id) {
		service.deleteRole(id);
	}

}
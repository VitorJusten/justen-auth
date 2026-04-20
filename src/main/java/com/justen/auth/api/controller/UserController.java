package com.justen.auth.api.controller;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.justen.auth.domain.model.dto.UserDto;
import com.justen.auth.domain.model.dto.input.UserAuthInputDto;
import com.justen.auth.domain.model.dto.input.UserInputDto;
import com.justen.auth.domain.service.UserService;

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
@RequestMapping("/user")
public class UserController {

	private final UserService service;
	
	@PostMapping
		public UserDto signUp(@RequestBody UserAuthInputDto model) {
		return new UserDto(service.create(model.toEntity()));
	}
	
	@PostMapping("/create")
	public UserDto create(@RequestBody UserInputDto model) {
		return new UserDto(service.create(model.toEntity()));
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable UUID id) {
		service.delete(id);
	}

	@PutMapping("/{id}")
	public UserDto update(@PathVariable UUID id, @RequestBody UserInputDto model) {
		return new UserDto(service.update(id, model.toEntity()));
	}

	@GetMapping("/{id}")
	public UserDto getById(@PathVariable UUID id) {
		return new UserDto(service.getById(id));
	}

	@GetMapping("/username/{username}")
	public UserDto getByUsername(@PathVariable String username) {
		return new UserDto(service.getByUsername(username));
	}

	@GetMapping
	public Page<UserDto> getAll(
			Pageable pageable,
			@RequestParam(required = false) String filter,
			@RequestParam(required = false) String id,
			@RequestParam(required = false) String username,
			@RequestParam(required = false) String role,
			@RequestParam(required = false) Boolean accountLocked,
			@RequestParam(required = false) String createdAt,
			@RequestParam(required = false) String lastLoginAt,
			@RequestParam(required = false) String lockUntil,
			@RequestParam(required = false) String updatedAt) {

		return service.getAll(
				pageable,
				filter,
				id,
				username,
				role,
				accountLocked,
				createdAt,
				lastLoginAt,
				lockUntil,
				updatedAt);
	}

	@PatchMapping("/{id}/password")
	public void updatePassword(@PathVariable UUID id, @RequestParam String password) {
		service.updatePassword(id, password);
	}

	@PatchMapping("/{id}/lock")
	public void disable(@PathVariable UUID id, @RequestParam(required = false) OffsetDateTime until) {
		service.disableUser(id, until);
	}

	@PatchMapping("/{id}/unlock")
	public void unlock(@PathVariable UUID id) {
		service.enableUser(id);
	}

}
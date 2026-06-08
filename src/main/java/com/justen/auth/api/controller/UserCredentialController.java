package com.justen.auth.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.justen.auth.core.dto.UserCredentialDto;
import com.justen.auth.core.dto.input.UserCredentialInputDto;
import com.justen.auth.domain.service.UserCredentialService;

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
@RequestMapping("/user-credential")
public class UserCredentialController {

	private final UserCredentialService service;

	@PutMapping("edit/all")
	public List<UserCredentialDto> editAll(@RequestBody List<UserCredentialInputDto> userCredentials) {
		return service.editAll(userCredentials.stream()
				.map(UserCredentialInputDto::toEntity)
				.toList())
				.stream()
				.map(UserCredentialDto::new)
				.toList();
	}

	@GetMapping
	public List<UserCredentialDto> getAll() {
		return service.findAll().stream()
				.map(UserCredentialDto::new)
				.toList();
	}

	@GetMapping("/{id}")
	public UserCredentialDto getById(@PathVariable UUID id) {
		return new UserCredentialDto(service.findById(id));
	}

}

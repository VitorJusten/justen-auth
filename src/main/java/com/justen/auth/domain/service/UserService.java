package com.justen.auth.domain.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.justen.auth.core.utils.SecurityUtils;
import com.justen.auth.domain.enums.RoleEnum;
import com.justen.auth.domain.exception.EntityNotFoundException;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.model.dto.UserDto;
import com.justen.auth.domain.repository.UserRepository;

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
public class UserService implements UserDetailsService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final SecurityUtils securityUtils;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("userNotFound"));

		return user;
	}

	public Page<UserDto> getAll(Pageable pageable, String filter, String id, String username, String role,
			Boolean accountLocked, String createdAt, String lastLoginAt, String lockUntil, String updatedAt) {
		securityUtils.validateRoles(List.of(RoleEnum.ADM.getName(), RoleEnum.DEV.getName()));

		return userRepository.getAll(pageable, filter, id, username, role, accountLocked, createdAt, lastLoginAt,
				lockUntil, updatedAt);
	}

	public User getById(UUID id) {
		return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("userNotFound"));
	}

	public User getByUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("userNotFound"));
	}

	@Transactional
	public User create(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}

	@Transactional
	public User update(UUID id, User user) {
		if (!securityUtils.getLoggedUserId().equals(id.toString())) {
			securityUtils.validateRoles(List.of(RoleEnum.ADM.getName(), RoleEnum.DEV.getName()));
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		User existing = getById(id);

		BeanUtils.copyProperties(user, existing, "id", "createdAt");

		return userRepository.save(existing);
	}

	@Transactional
	public void delete(UUID id) {
		if (!securityUtils.getLoggedUserId().equals(id.toString())) {
			securityUtils.validateRoles(List.of(RoleEnum.ADM.getName(), RoleEnum.DEV.getName()));
		}
		userRepository.deleteById(id);
	}

	@Transactional
	public void updatePassword(UUID id, String newPassword) {
		if (!securityUtils.getLoggedUserId().equals(id.toString())) {
			securityUtils.validateRoles(List.of(RoleEnum.ADM.getName(), RoleEnum.DEV.getName()));
		}
		User user = getById(id);
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	@Transactional
	public void disableUser(UUID id, OffsetDateTime until) {
		if (!securityUtils.getLoggedUserId().equals(id.toString())) {
			securityUtils.validateRoles(List.of(RoleEnum.ADM.getName(), RoleEnum.DEV.getName()));
		}

		User user = getById(id);

		user.setAccountLocked(true);
		user.setLockUntil(until);

		userRepository.save(user);
	}

	@Transactional
	public void enableUser(UUID id) {
		if (!securityUtils.getLoggedUserId().equals(id.toString())) {
			securityUtils.validateRoles(List.of(RoleEnum.ADM.getName(), RoleEnum.DEV.getName()));
		}

		User user = getById(id);

		user.setAccountLocked(false);
		user.setLockUntil(null);

		userRepository.save(user);
	}

}

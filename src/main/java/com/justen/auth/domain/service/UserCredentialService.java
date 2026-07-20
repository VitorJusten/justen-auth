package com.justen.auth.domain.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.core.enums.CredentialTypeEnum;
import com.justen.auth.core.utils.SecurityUtils;
import com.justen.auth.domain.exception.BusinessException;
import com.justen.auth.domain.model.UserCredential;
import com.justen.auth.domain.repository.UserCredentialRepository;

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
@Transactional
public class UserCredentialService {

	private final UserCredentialRepository repository;
	private final SecurityUtils securityUtils;

	@Transactional()
	public List<UserCredential> editAll(List<UserCredential> userCredentials) {

		UUID userId = securityUtils.getLoggedUserId();

		validateCredentials(userCredentials);

		List<UserCredential> credentials = findByUserId(userId);

		if (credentials != null && !credentials.isEmpty()) {
			for (UserCredential credential : credentials) {
				boolean existsInParam = userCredentials.stream()
						.anyMatch(paramCredential -> credential.getCredentialType()
								.equals(paramCredential.getCredentialType()));

				if (!existsInParam) {
					repository.delete(credential);
				}
			}
		}

		List<UserCredential> credentialsToSave = new ArrayList<>();

		for (UserCredential paramCredential : userCredentials) {
			UserCredential credential = repository
					.findByUserIdAndCredentialType(userId, paramCredential.getCredentialType())
					.orElseGet(UserCredential::new);

			if (credential.getId() == null) {
				credential.setId(UUID.randomUUID());
			}

			BeanUtils.copyProperties(paramCredential, credential, "id", "userId");
			credential.setUserId(getUserIdReference(userId));

			credentialsToSave.add(credential);
		}

		repository.saveAll(credentialsToSave);

		return findByUserId(userId);
	}

	private void validateCredentials(List<UserCredential> userCredentials) {

		Set<CredentialTypeEnum> credentialTypes = EnumSet.noneOf(CredentialTypeEnum.class);

		for (UserCredential credential : userCredentials) {
			if (!credentialTypes.add(credential.getCredentialType())) {
				throw new BusinessException(
						"Duplicate credential type found: " + credential.getCredentialType());
			}
		}
	}

	private UUID getUserIdReference(UUID userId) {
		return userId;
	}

	@Transactional()
	public UserCredential findById(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> new BusinessException("UserCredential not found: " + id));
	}

	@Transactional()
	public List<UserCredential> findAll() {
		return repository.findAll();
	}

	@Transactional()
	public List<UserCredential> findByUserId(UUID userId) {
		return repository.findByUserId(userId);
	}

	/**
	 * Find By Credential
	 */
	@Transactional(readOnly = true)
	public UserCredential findByCredential(String credential) {
		return repository.findByCredential(credential)
				.orElseThrow(() -> new BusinessException(
						"Credential not found: " + credential));
	}

}

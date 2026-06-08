package com.justen.auth.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.justen.auth.core.enums.CredentialTypeEnum;
import com.justen.auth.domain.model.UserCredential;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {

	/**
	 * 
	 * @param userId
	 * @return
	 */
	@Query("""
			    SELECT uc
			    FROM UserCredential uc
			    WHERE uc.user.id = :userId
			""")
	List<UserCredential> findByUserId(@Param("userId") UUID userId);

	/**
	 * 
	 * @param userId
	 * @param credentialType
	 * @return
	 */
	@Query("""
			    SELECT uc
			    FROM UserCredential uc
			    WHERE uc.user.id = :userId
			    AND uc.credentialType = :credentialType
			""")
	Optional<UserCredential> findByUserIdAndCredentialType(
			@Param("userId") UUID userId,
			@Param("credentialType") CredentialTypeEnum credentialType);
	
	/**
	 * 
	 * @param credential
	 * @return
	 */
	@Query("""
		    SELECT uc
		    FROM UserCredential uc
		    WHERE uc.credential = :credential
		""")
		Optional<UserCredential> findByCredential(
		        @Param("credential") String credential);

}

package com.justen.auth.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
			    FROM UserCredentials uc
			    WHERE uc.user.id = :userId
			""")
	List<UserCredential> findByUserId(@Param("userId") UUID userId);
	
	/**
	 * 
	 * @param credential
	 * @return
	 */
	@Query("""
		    SELECT uc
		    FROM UserCredentials uc
		    WHERE uc.credential = :credential
		""")
		Optional<UserCredential> findByCredential(
		        @Param("credential") String credential);

}

package com.justen.auth.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.custom.UserRepositoryCustom;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {

	@Query(value = """
			    SELECT *
			    FROM user_account
			    WHERE usac_tx_username = :username
			""", nativeQuery = true)
	Optional<User> findByUsername(@Param(value = "username") String username);

	/**
	 * 
	 * @param credential
	 * @return
	 */
	@Query(value = """
		    SELECT u.*
		    FROM user_account u
		    JOIN user_credential uc ON uc.usac_cd_id = u.usac_cd_id
		    WHERE u.usac_tx_username = :credential 
		    OR uc.uscr_tx_credential = :credential
		""", nativeQuery = true)
	Optional<User> findByCredential(@Param(value = "credential") String credential);

}

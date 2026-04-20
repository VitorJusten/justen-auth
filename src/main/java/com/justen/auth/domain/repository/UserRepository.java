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

}

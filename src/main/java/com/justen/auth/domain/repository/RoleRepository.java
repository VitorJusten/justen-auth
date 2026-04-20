package com.justen.auth.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.justen.auth.domain.model.Role;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
public interface RoleRepository extends JpaRepository<Role, UUID> {

}

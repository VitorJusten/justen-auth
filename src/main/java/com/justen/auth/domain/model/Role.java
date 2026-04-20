package com.justen.auth.domain.model;

import java.util.UUID;

import com.justen.auth.domain.enums.RoleEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */

@Data
@Entity
@Table(name = "role")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class Role {

	@Id
	@Column(name = "role_cd_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	@EqualsAndHashCode.Include
	private UUID id;

	@Column(name = "role_tx_name", unique = true, nullable = false)
	private String name;

	public Role(RoleEnum role) {
		this.id = role.getId();
		this.name = role.getName();
		System.out.println(role);
	}
	
}

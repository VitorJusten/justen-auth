package com.justen.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Data
@Entity
@Table(name = "user_account")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "usac_cd_id")
	@EqualsAndHashCode.Include
	private UUID id;

	@Column(name = "usac_tx_username", unique = true, nullable = false)
	private String username;

	@Column(name = "usac_tx_password", nullable = false)
	private String password;

	@Column(name = "usac_nm_account_locked", nullable = false)
	private Boolean accountLocked = false;

	@Column(name = "usac_nm_failed_login_attempts")
	private Integer failedLoginAttempts = 0;

	@Column(name = "usac_dt_lock_until")
	private OffsetDateTime lockUntil;

	@Column(name = "usac_dt_last_login_at")
	private OffsetDateTime lastLoginAt;

	@Column(name = "usac_dt_created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "usac_dt_updated_at")
	private OffsetDateTime updatedAt;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_role",
		joinColumns = @JoinColumn(name = "usac_cd_id"),
		inverseJoinColumns = @JoinColumn(name = "role_cd_id"))
	private Set<Role> roles = new HashSet<>();

	@PrePersist
	public void prePersist() {
		this.createdAt = OffsetDateTime.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
        		.map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
        		.toList();
    }

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		if (Boolean.TRUE.equals(accountLocked)) {
			if (lockUntil == null) {
				return false;
			}
			return lockUntil.isBefore(OffsetDateTime.now());
		}
		
		if (lockUntil != null) {
			return lockUntil.isBefore(OffsetDateTime.now());
		}
		
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		if (lockUntil != null) {
			return lockUntil.isBefore(OffsetDateTime.now());
		}
		return !Boolean.TRUE.equals(accountLocked);
	}

}

package com.justen.auth.core.utils;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Component("coreSecurityUtils")
@AllArgsConstructor
public class SecurityUtils {
	public void validateRoles(List<String> of) {
		System.out.println("CONFIGURAR validateRoles");
	}

	public Object getLoggedUserId() {
		System.out.println("CONFIGURAR getLoggedUserId");
		return UUID.randomUUID();
	}

}

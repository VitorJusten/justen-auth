package com.justen.auth.api.security.listener;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.justen.auth.domain.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

/**
 * Atualiza contadores de login/lock com base nos eventos do Spring Security.
 */
@Component
@RequiredArgsConstructor
public class AuthenticationEventsListener {

	private final AuthenticationService authenticationService;

	@EventListener
	public void onSuccess(AuthenticationSuccessEvent event) {
		if (event.getAuthentication() instanceof UsernamePasswordAuthenticationToken auth) {
			authenticationService.loginSucceeded(auth.getName());
		}
	}

	@EventListener
	public void onFailure(AbstractAuthenticationFailureEvent event) {
		if (event.getAuthentication() instanceof UsernamePasswordAuthenticationToken auth) {
			authenticationService.loginFailed(auth.getName());
		}
	}
}

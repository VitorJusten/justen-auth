package com.justen.auth.api.security.handler;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.justen.auth.domain.model.User;
import com.justen.auth.domain.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

	private static final DateTimeFormatter LOCK_UNTIL_FORMATTER =
			DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a XXX", Locale.ENGLISH);

	private final UserRepository userRepository;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws java.io.IOException, ServletException {

		String username = request.getParameter("username");
		User lockedUser = StringUtils.isBlank(username) ? null
				: userRepository.findByUsername(username)
						.filter(user -> !user.isAccountNonLocked())
						.orElse(null);

		if (exception instanceof LockedException || lockedUser != null) {
			String redirectUrl = UriComponentsBuilder.fromPath("/login")
					.queryParam("locked", "1")
					.queryParamIfPresent("lockUntil", java.util.Optional.ofNullable(formatLockUntil(lockedUser)))
					.build()
					.toUriString();

			response.sendRedirect(redirectUrl);
			return;
		}

		response.sendRedirect("/login?error=1");
	}

	private String formatLockUntil(User user) {
		if (user == null) {
			return null;
		}

		OffsetDateTime lockUntil = user.getLockUntil();
		return lockUntil != null ? LOCK_UNTIL_FORMATTER.format(lockUntil) : null;
	}
}

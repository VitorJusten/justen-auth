package com.justen.auth.api.security.config;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.justen.auth.api.security.handler.LoginFailureHandler;
import com.justen.auth.infrastructure.AuthUserProperties;

import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@AllArgsConstructor
public class ResourceServerConfig {

	private final AuthUserProperties properties;
	private final CorsConfig corsConfig;
	private final LoginFailureHandler loginFailureHandler;

	private static final String[] WHITE_LIST = {
			"/actuator/**",
			"/v3/api-docs/**",
			"/favicon.ico",
			"/fonts/Anja_Eliane.ttf",
			"/login",
			"/error",
			"/css/**", "/js/**", "/images/**", "/webjars/**"
	};

	@Bean
	SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
		final String REDIRECT_URI = StringUtils.join(properties.getUiUrl());

		http
				.exceptionHandling(ex -> ex.accessDeniedHandler(accessDenied())) // Acess handler
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(WHITE_LIST).permitAll() // Permitir URLs da white list
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/user").permitAll()
						.anyRequest().access((authentication, context) -> {
							var authorities = authentication.get().getAuthorities();
							boolean hasScope = authorities.stream().anyMatch(
									a -> a.getAuthority().equals("SCOPE_" + properties.getAuthorization().getScope()));
							boolean hasAnyRole = authorities.stream()
									.anyMatch(a -> a.getAuthority().startsWith("ROLE_"));
							boolean isCommonUser = authentication.get().isAuthenticated() && !hasAnyRole;
							return new AuthorizationDecision(hasScope || hasAnyRole || isCommonUser);
						}))
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/oauth2/**", "/actuator/**", "/v3/api-docs/**", "/login",
								"/authentication/**", "/user/**")
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
				.logout(logout -> logout
						.deleteCookies("JSESSIONID") // Deletar cookie de sessão ao sair
						.logoutSuccessUrl(REDIRECT_URI) // Redirecionar após logout
				)
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())) // Configurar JWT
				);

		corsConfig.corsCustomizer(http); // Customizador de CORS

		return http.formLogin(form -> form
				.loginPage("/login") // Página de login customizada
				.defaultSuccessUrl(REDIRECT_URI, true) // Sucesso no login
				.failureHandler(resourceAuthenticationFailureHandler())
				.permitAll() // Permitir todos para a página de login
		).build();

	}

	@Bean
	AuthenticationFailureHandler resourceAuthenticationFailureHandler() {
		return loginFailureHandler;
	}

	private AccessDeniedHandler accessDenied() {
		return (request, response, accessDeniedException) -> {
			Cookie cookie = new Cookie("JSESSIONID", null);
			cookie.setMaxAge(-1);

			response.addCookie(cookie);
			response.setStatus(403);
		};
	}

	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
			Collection<GrantedAuthority> grandAuthorities = authoritiesConverter.convert(jwt);

			List<String> roles = jwt.getClaimAsStringList("roles");
			if (roles != null) {
				grandAuthorities.addAll(roles.stream().map(SimpleGrantedAuthority::new).toList());
			}

			return grandAuthorities;
		});

		return converter;
	}

}

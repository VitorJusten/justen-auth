package com.justen.auth.api.security.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import com.justen.auth.api.security.handler.LoginFailureHandler;
import com.justen.auth.domain.model.User;
import com.justen.infrastructure.AppProperties;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.AllArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Configuration
@AllArgsConstructor
public class AuthorizationServerConfig {

	private final CorsConfig corsConfig;
	private final AppProperties properties;
	private final PasswordEncoder passwordEncoder;
	private final LoginFailureHandler loginFailureHandler;

	@SuppressWarnings("removal")
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

		corsConfig.corsCustomizer(http);

		return http.formLogin(form -> form
				.failureHandler(authServerFailureHandler()))
				.build();
	}

	@Bean
	AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder()
				.issuer(properties.getIssuer())
				.build();
	}

	@Bean
	RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
		JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);

		registerDefaultClients(repository);

		return repository;
	}

	private void registerDefaultClients(RegisteredClientRepository repository) {
		RegisteredClient authClient = buildAuthClient();
		if (repository.findByClientId(authClient.getClientId()) == null) {
			repository.save(authClient);
		}

		RegisteredClient apiClient = buildApiClient();
		if (repository.findByClientId(apiClient.getClientId()) == null) {
			repository.save(apiClient);
		}
	}

	private RegisteredClient buildAuthClient() {
		final String redirectUri = StringUtils.appendIfMissing(properties.getUiUrl(), "/") + "login/oauth2/code";
		final String postLogoutRedirect = properties.getUiUrl();

		return RegisteredClient
				.withId("auth-" + properties.getAuthorization().getClientId())
				.clientId(properties.getAuthorization().getClientId())
				.clientSecret(passwordEncoder.encode(properties.getAuthorization().getClientSecret()))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.scope(properties.getAuthorization().getScope())
				.tokenSettings(defaultTokenSettings())
				.redirectUri(redirectUri)
				.postLogoutRedirectUri(postLogoutRedirect)
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
				.build();
	}

	private RegisteredClient buildApiClient() {
		return RegisteredClient
				.withId("api-" + properties.getApi().getClientId())
				.clientId(properties.getApi().getClientId())
				.clientSecret(passwordEncoder.encode(properties.getApi().getClientSecret()))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.scope(properties.getApi().getScope())
				.tokenSettings(defaultTokenSettings())
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
				.build();
	}

	private TokenSettings defaultTokenSettings() {
		Integer accessTtl = properties.getTimer() != null ? properties.getTimer().getAccessTokenTTL() : null;
		Integer refreshTtl = properties.getTimer() != null ? properties.getTimer().getRefreshTokenTTL() : null;

		return TokenSettings.builder()
				.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
				.accessTokenTimeToLive(Duration.ofMinutes(accessTtl != null ? accessTtl : 5))
				.reuseRefreshTokens(false)
				.refreshTokenTimeToLive(Duration.ofMinutes(refreshTtl != null ? refreshTtl : 10))
				.build();
	}

	@Bean
	JWKSource<SecurityContext> jwkSource() {
		RSAKey rsaKey = loadRsaKey();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return new ImmutableJWKSet<>(jwkSet);
	}

	@Bean
	org.springframework.security.web.authentication.AuthenticationFailureHandler authServerFailureHandler() {
		return loginFailureHandler;
	}

	private RSAKey loadRsaKey() {
		AppProperties.Jwt jwt = properties.getJwt();

		try {
			if (jwt != null && StringUtils.isNotBlank(jwt.getPrivateKey())) {
				JWK jwk = JWK.parseFromPEMEncodedObjects(jwt.getPrivateKey());
				if (!(jwk instanceof RSAKey rsa)) {
					throw new IllegalStateException("Provided key is not RSA");
				}
				if (rsa.getKeyID() == null) {
					rsa = new RSAKey.Builder(rsa).keyID(UUID.randomUUID().toString()).build();
				}
				return rsa;
			}
		} catch (Exception ex) {
			throw new IllegalStateException("Could not parse RSA keys from properties", ex);
		}

		KeyPair keyPair = generateRsaKey();
		return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
				.privateKey((RSAPrivateKey) keyPair.getPrivate())
				.keyID(UUID.randomUUID().toString())
				.build();
	}

	private static KeyPair generateRsaKey() {
		KeyPair keyPair;

		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} 
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

	@Bean
	OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
		return context -> {

			Authentication authentication = context.getPrincipal();

			if (authentication.getPrincipal() instanceof User) {
				User principal = (User) authentication.getPrincipal();
				var roles = principal.getRoles().stream()
						.map(r -> "ROLE_" + r.getName())
						.toList();

				var claims = context.getClaims()
						.claim("user_id", principal.getId().toString())
						.claim("user_name", principal.getUsername())
						.claim("roles", roles)
						.id(UUID.randomUUID().toString());
			}
		};
	}

}

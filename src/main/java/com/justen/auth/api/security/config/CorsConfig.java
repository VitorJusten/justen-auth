package com.justen.auth.api.security.config;

import java.util.List;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.justen.infrastructure.AppProperties;

import lombok.AllArgsConstructor;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Component
@AllArgsConstructor
public class CorsConfig {

	private final AppProperties properties;

	public void corsCustomizer(HttpSecurity http) throws Exception {
		http.cors(corsCustomizer -> {

			CorsConfigurationSource source = s -> {
				CorsConfiguration corsConfig = new CorsConfiguration();
				corsConfig.setAllowCredentials(true);
				List<String> allowed = properties.getCors().getAllowedOrigins();
				if (allowed == null || allowed.isEmpty()) {
					corsConfig.setAllowedOriginPatterns(List.of("*"));
				} else {
					corsConfig.setAllowedOrigins(allowed);
				}
				corsConfig.setAllowedHeaders(List.of("*"));
				corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
				return corsConfig;
			};

			corsCustomizer.configurationSource(source);
		});
	}
	
}

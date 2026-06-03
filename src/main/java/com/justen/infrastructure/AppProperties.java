package com.justen.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
@Getter
@Setter
public class AppProperties {

	private long expiration = 900; // 15 minutos em segundos
	private long refreshExpiration = 604800; // 7 dias em segundos
	private String issuer = "justen-auth";
	private String audience = "justen-api";

}

package com.justen.auth.core.config;

import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Configuration
public class HttpTraceConfig {
	
	@Bean
	public HttpExchangeRepository httpTraceRepository() {
		InMemoryHttpExchangeRepository inMemoryHttpExchangeRepository = new InMemoryHttpExchangeRepository();
		inMemoryHttpExchangeRepository.setCapacity(1000);
		return inMemoryHttpExchangeRepository;
	}

}

package com.justen.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AppProperties {

    private String uiUrl;
    
    private String issuer = "http://localhost:8081";

    private Cors cors = new Cors();

    private Timer timer;

    private Authorization authorization;

    private Api api;

    private Jwt jwt;

    @Data
    public static class Cors {
        private java.util.List<String> allowedOrigins = java.util.List.of();
    }

    @Data
    public static class Timer {

        private Integer accessTokenTTL;

        private Integer refreshTokenTTL;

    }

    @Data
    public static class Authorization {

        private String clientId;

        private String clientSecret;

        private String scope;

    }

    @Data
    public static class Api {

        private String clientId;

        private String clientSecret;

        private String scope;

    }

    @Data
    public static class Jwt {
        private String publicKey;
        private String privateKey;
    }

}

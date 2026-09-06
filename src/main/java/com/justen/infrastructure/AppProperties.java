package com.justen.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Data
@Component
@ConfigurationProperties
public class AppProperties {

    private Server server;
    private Spring spring;
    private Auth auth;

    @Data
    public static class Server {
        private Integer port;
    }

    @Data
    public static class Spring {
        private Application application;
        private Datasource datasource;
        private Liquibase liquibase;
        private Jpa jpa;
    }

    @Data
    public static class Application {
        private String name;
    }

    @Data
    public static class Datasource {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
    }

    @Data
    public static class Liquibase {
        private String changeLog;
    }

    @Data
    public static class Jpa {
        private String databasePlatform;
        private Hibernate hibernate;
        private Boolean showSql;
    }

    @Data
    public static class Hibernate {
        private String ddlAuto;
    }

    @Data
    public static class Auth {
        private Long expiration = 900L;
        private Long refreshExpiration = 604800L;
        private String issuer = "justen-auth";
        private String audience = "justen-api";
        private String defaultPassword = "justen@123";
        private String origins = "http://localhost:3000";
        private KeyConfig keys = new KeyConfig();
        private GoogleConfig google = new GoogleConfig();
        private SteamConfig steam = new SteamConfig();
        private MfaConfig mfa = new MfaConfig();
        private RateLimitConfig rateLimit = new RateLimitConfig();

        public java.util.List<String> getOriginsList() {
            if (origins == null || origins.isBlank()) {
                return java.util.List.of("http://localhost:3000");
            }
            return java.util.Arrays.stream(origins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    @Data
    public static class KeyConfig {
        private String algorithm = "RS256";
        private Integer keySize = 2048;
        private Long rotationDays = 90L;
        private Long gracePeriodDays = 7L;
    }

    @Data
    public static class GoogleConfig {
        private String clientId = "";
    }

    @Data
    public static class SteamConfig {
        private String apiKey = "";
        private String realm = "http://localhost:8081";
    }

    @Data
    public static class MfaConfig {
        private String issuer = "JustenAuth";
        private Integer codeLength = 6;
        private Integer timeStepSeconds = 30;
        private Integer window = 1;
        private Integer recoveryCodesCount = 8;
    }

    @Data
    public static class RateLimitConfig {
        private Boolean enabled = true;
        private Integer requestsPerMinute = 60;
        private Integer loginRequestsPerMinute = 15;
    }
}
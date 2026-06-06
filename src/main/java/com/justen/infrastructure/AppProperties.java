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
        private Long expiration;
        private Long refreshExpiration;
        private String issuer;
        private String audience;
    }
}
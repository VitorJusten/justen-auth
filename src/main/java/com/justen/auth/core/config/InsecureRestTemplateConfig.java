package com.justen.auth.core.config;

import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import jakarta.annotation.PostConstruct;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Configuration
@Profile("insecure")
public class InsecureRestTemplateConfig {

	@Value("${VERTIS_JWK_SET_URI:http://localhost:8081/oauth2/jwks}")
	private String jwkSetUri;
	
	
	/**
	 * Configura o SSL para aceitar todos os certificados, incluindo autoassinados e não confiáveis.
	 * 
	 * Este método sobrescreve a configuração global de SSL, desabilitando a verificação de hostname
	 * e a validação de certificados. Isso permite que chamadas HTTPS sejam feitas mesmo com certificados inválidos.
	 * 
	 * 
	 * @throws Exception se ocorrer alguma falha na configuração do SSLContext.
	 */
    @PostConstruct
    public void configureInsecureSSL() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}

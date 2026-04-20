package com.justen.auth.core.config;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Configuration
@Profile("insecure")
public class SSLConfig {

	@Bean
    RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509ExtendedTrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null;}
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            public void checkClientTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {}
            public void checkServerTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {}
            public void checkClientTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {}
            public void checkServerTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {}

        } };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HostnameVerifier allHostsValid = (hostname, session) -> true;

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create().setSslContext(sslContext).build();
        final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslSocketFactory).build();
        CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).evictExpiredConnections().build();
        
        ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);
        return new RestTemplate(factory );
    }
}

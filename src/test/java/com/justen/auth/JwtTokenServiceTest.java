package com.justen.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;

import com.justen.auth.domain.model.OAuthClient;
import com.justen.auth.domain.model.Role;
import com.justen.auth.domain.model.User;
import com.justen.auth.domain.service.JwtService;
import com.justen.auth.domain.service.KeyManagementService;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private KeyManagementService keyManagementService;

    @Test
    void shouldGenerateAndValidateUserAccessToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        Role role = new Role();
        role.setName("USER");
        user.getRoles().add(role);

        String token = jwtService.generateAccessToken(user, "test-client", List.of("read", "write"));
        assertNotNull(token);

        Jwt decoded = jwtService.decode(token);
        assertEquals(user.getId().toString(), decoded.getSubject());
        assertEquals("testuser", decoded.getClaimAsString("username"));
        assertEquals("test-client", decoded.getClaimAsString("client_id"));
        assertEquals(keyManagementService.getActiveRsaKey().getKeyID(), decoded.getHeaders().get("kid"));
        assertTrue(decoded.getExpiresAt().isAfter(decoded.getIssuedAt()));
    }

    @Test
    void shouldGenerateClientCredentialsToken() {
        OAuthClient client = new OAuthClient();
        client.setId(UUID.randomUUID());
        client.setClientId("microservice-client");
        client.setScopes("read,write");

        String token = jwtService.generateClientCredentialsToken(client, List.of("read", "write"));
        assertNotNull(token);

        Jwt decoded = jwtService.decode(token);
        assertEquals("microservice-client", decoded.getSubject());
        assertEquals("microservice-client", decoded.getClaimAsString("client_id"));
    }
}

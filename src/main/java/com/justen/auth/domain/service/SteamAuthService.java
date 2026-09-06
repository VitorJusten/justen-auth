package com.justen.auth.domain.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.justen.auth.core.dto.ExternalUserInfo;
import com.justen.auth.domain.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SteamAuthService {

    private static final String STEAM_LOGIN_URL = "https://steamcommunity.com/openid/login";
    private static final Pattern STEAM_ID_PATTERN = Pattern.compile(".*/id/(\\d+)$");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public ExternalUserInfo verifySteamLogin(Map<String, String> openIdParams) {
        String claimedId = openIdParams.get("openid.claimed_id");
        if (claimedId == null || claimedId.isBlank()) {
            throw new BusinessException("Missing openid.claimed_id in Steam response");
        }

        Matcher matcher = STEAM_ID_PATTERN.matcher(claimedId);
        if (!matcher.matches()) {
            throw new BusinessException("Invalid Steam claimed ID format: " + claimedId);
        }
        String steamId64 = matcher.group(1);

        // Prepara os parâmetros para validação check_authentication contra o Steam
        Map<String, String> verifyParams = openIdParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("openid."))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        verifyParams.put("openid.mode", "check_authentication");

        String formBody = verifyParams.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(STEAM_LOGIN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || !response.body().contains("is_valid:true")) {
                log.warn("Steam check_authentication failed. Response: {}", response.body());
                throw new BusinessException("Steam authentication validation failed");
            }

            return ExternalUserInfo.builder()
                    .provider("STEAM")
                    .providerUserId(steamId64)
                    .displayName("SteamUser_" + steamId64)
                    .emailVerified(false)
                    .build();

        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("Failed to communicate with Steam OpenID server", ex);
            throw new BusinessException("Error validating Steam authentication");
        }
    }
}

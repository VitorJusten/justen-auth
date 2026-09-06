package com.justen.auth.domain.service;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import com.justen.infrastructure.AppProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TotpService {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base32 BASE32 = new Base32();

    private final AppProperties appProperties;

    public String generateSecretKey() {
        byte[] buffer = new byte[20];
        SECURE_RANDOM.nextBytes(buffer);
        return BASE32.encodeToString(buffer).replace("=", "");
    }

    public String generateOtpAuthUri(String secret, String accountName) {
        String issuer = appProperties.getAuth().getMfa().getIssuer() != null
                ? appProperties.getAuth().getMfa().getIssuer()
                : "JustenAuth";

        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String encodedAccount = URLEncoder.encode(accountName, StandardCharsets.UTF_8);

        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                encodedIssuer, encodedAccount, secret, encodedIssuer);
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }

        String cleanedCode = code.trim().replaceAll("\\s+", "");
        if (cleanedCode.length() != 6 || !cleanedCode.matches("\\d{6}")) {
            return false;
        }

        int expectedCode;
        try {
            expectedCode = Integer.parseInt(cleanedCode);
        } catch (NumberFormatException e) {
            return false;
        }

        int timeStep = appProperties.getAuth().getMfa().getTimeStepSeconds() != null
                ? appProperties.getAuth().getMfa().getTimeStepSeconds()
                : 30;

        int window = appProperties.getAuth().getMfa().getWindow() != null
                ? appProperties.getAuth().getMfa().getWindow()
                : 1;

        long currentInterval = Instant.now().getEpochSecond() / timeStep;

        for (int i = -window; i <= window; i++) {
            long hash = generateTotpForInterval(secret, currentInterval + i);
            if (hash == expectedCode) {
                return true;
            }
        }

        return false;
    }

    private int generateTotpForInterval(String secret, long interval) {
        byte[] key = BASE32.decode(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(interval).array();

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            return binary % 1_000_000;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Error computing TOTP HMAC", e);
        }
    }
}

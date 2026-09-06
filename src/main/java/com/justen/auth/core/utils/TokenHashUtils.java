package com.justen.auth.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class TokenHashUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenHashUtils() {
    }

    /**
     * Gera um token opaco criptograficamente seguro (256 bits / 32 bytes)
     */
    public static String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(bufferToFill(randomBytes));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private static byte[] bufferToFill(byte[] buf) {
        SECURE_RANDOM.nextBytes(buf);
        return buf;
    }

    /**
     * Calcula o hash SHA-256 de uma string e retorna em hexadecimal minúsculo
     */
    public static String sha256Hex(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}

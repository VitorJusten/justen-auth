package com.justen.auth.core.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Data
@Builder
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
}

package com.justen.auth.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalUserInfo {
    private String provider;
    private String providerUserId;
    private String email;
    private String displayName;
    private boolean emailVerified;
}

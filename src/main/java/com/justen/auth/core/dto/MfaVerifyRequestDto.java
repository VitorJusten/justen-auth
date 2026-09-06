package com.justen.auth.core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaVerifyRequestDto {
    @NotBlank(message = "MFA challenge token is required")
    private String mfaToken;

    @NotBlank(message = "Verification code is required")
    private String code;
}

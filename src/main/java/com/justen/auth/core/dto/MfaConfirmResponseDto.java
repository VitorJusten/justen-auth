package com.justen.auth.core.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaConfirmResponseDto {
    private boolean enabled;
    private List<String> recoveryCodes;
    private String message;
}

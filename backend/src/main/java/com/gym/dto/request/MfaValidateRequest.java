package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MfaValidateRequest(
    @NotBlank(message = "Pre-auth token is required")
    String preAuthToken,
    
    @NotBlank(message = "Code is required")
    String code
) {}
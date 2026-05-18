package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MfaVerifyRequest(
    @NotBlank(message = "Code is required")
    String code
) {}
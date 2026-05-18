package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MfaSetupRequest(
    @NotBlank(message = "Password is required")
    String password
) {}
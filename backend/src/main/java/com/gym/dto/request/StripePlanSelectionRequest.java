package com.gym.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StripePlanSelectionRequest(
    @NotNull(message = "Plan ID is required")
    UUID planId
) {}

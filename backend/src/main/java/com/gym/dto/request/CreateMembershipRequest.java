package com.gym.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateMembershipRequest(
    @NotNull(message = "User ID is required")
    UUID userId,

    @NotNull(message = "Plan ID is required")
    UUID planId,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    Boolean autoRenew
) {}
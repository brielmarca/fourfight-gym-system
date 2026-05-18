package com.gym.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateScheduleRequestRequest(
    @NotNull(message = "Trainer ID is required")
    UUID trainerId,

    LocalDateTime preferredAt,

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    String notes
) {}
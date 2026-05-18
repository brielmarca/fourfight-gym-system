package com.gym.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateTrainerRequest(
    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    String bio,

    String specialties,

    BigDecimal rating,

    @Min(value = 1, message = "Max clients must be at least 1")
    @Max(value = 50, message = "Max clients must not exceed 50")
    Integer maxClients,

    Boolean isActive
) {}
package com.gym.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateGraduationRequest(
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name,

    @Min(value = 1, message = "Level order must be at least 1")
    Integer levelOrder,

    UUID martialArtId
) {}

package com.gym.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateGraduationRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name,

    @NotNull(message = "Level order is required")
    @Min(value = 1, message = "Level order must be at least 1")
    Integer levelOrder,

    @NotNull(message = "Martial art ID is required")
    UUID martialArtId
) {}

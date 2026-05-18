package com.gym.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateMartialArtRequest(
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name
) {}

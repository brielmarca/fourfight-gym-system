package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBeltRequest(
    @NotBlank @Size(max = 50) String name,
    @Size(max = 7) String colorHex,
    @NotBlank Integer rankOrder
) {}
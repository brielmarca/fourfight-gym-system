package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminUpdateStudentGraduationRequest(
    @NotBlank(message = "Email is required")
    String studentEmail,

    @NotBlank(message = "Modality is required")
    @Pattern(regexp = "^(JIU_JITSU|BOXE_KICKBOXING|CAPOEIRA|MMA)$", message = "Invalid modality")
    String modality,

    @NotBlank(message = "Current level is required")
    String currentLevel
) {}

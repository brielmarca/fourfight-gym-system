package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AdminUpdateStudentGraduationByUserRequest(
    @NotBlank(message = "Modality is required")
    @Pattern(regexp = "^(JIU_JITSU|BOXE_KICKBOXING|CAPOEIRA|MMA)$", message = "Invalid modality")
    String modality,

    @NotNull(message = "Graduation is required")
    UUID graduationId,

    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason must be at most 1000 characters")
    String reason
) {}

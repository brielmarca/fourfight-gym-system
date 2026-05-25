package com.gym.dto.request;

import com.gym.entity.TeachingModality;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateVideoLessonRequest(
    @NotBlank @Size(max = 255) String title,
    @Size(max = 4000) String description,
    @NotNull TeachingModality modality,
    @NotBlank @Size(max = 2000) String videoUrl,
    @NotNull @Min(1) @Max(3) Integer minimumPlanRank,
    UUID professorId,
    Boolean active
) {}

package com.gym.dto.request;

import com.gym.entity.TeachingModality;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AdminCreateProfessorAssignmentRequest(
    @NotNull UUID professorId,
    @NotNull UUID studentId,
    @NotNull TeachingModality modality,
    @Size(max = 1000) String notes
) {}

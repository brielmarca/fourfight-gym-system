package com.gym.dto.request;

import com.gym.entity.TeachingModality;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record AdminUpdateProfessorModalitiesRequest(
    @NotEmpty Set<@NotNull TeachingModality> modalities
) {}

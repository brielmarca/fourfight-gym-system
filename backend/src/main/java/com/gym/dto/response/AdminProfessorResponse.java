package com.gym.dto.response;

import com.gym.entity.TeachingModality;
import java.util.Set;
import java.util.UUID;

public record AdminProfessorResponse(
    UUID professorId,
    String professorName,
    String professorEmail,
    Set<TeachingModality> modalities
) {}

package com.gym.dto.response;

import com.gym.entity.TeachingModality;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminProfessorAssignmentResponse(
    UUID assignmentId,
    String professorName,
    String professorEmail,
    String studentName,
    String studentEmail,
    TeachingModality modality,
    Boolean active,
    String notes,
    LocalDateTime assignedAt,
    LocalDateTime updatedAt
) {}

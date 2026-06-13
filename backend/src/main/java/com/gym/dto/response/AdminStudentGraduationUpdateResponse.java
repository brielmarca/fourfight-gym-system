package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminStudentGraduationUpdateResponse(
    UUID userId,
    String studentEmail,
    String studentName,
    String modality,
    String oldGraduation,
    String newGraduation,
    LocalDateTime updatedAt
) {}

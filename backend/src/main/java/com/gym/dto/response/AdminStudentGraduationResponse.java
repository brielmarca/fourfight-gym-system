package com.gym.dto.response;

import java.time.LocalDateTime;

public record AdminStudentGraduationResponse(
    String studentName,
    String studentEmail,
    String modality,
    String currentGraduation,
    String nextGraduation,
    LocalDateTime updatedAt
) {}

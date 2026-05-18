package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Student;

public record StudentResponse(
    UUID id,
    String name,
    String email,
    UUID planId,
    String planName,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
            student.getId(),
            student.getName(),
            student.getEmail(),
            student.getPlan() != null ? student.getPlan().getId() : null,
            student.getPlan() != null ? student.getPlan().getName() : null,
            student.getIsActive(),
            student.getCreatedAt(),
            student.getUpdatedAt()
        );
    }
}

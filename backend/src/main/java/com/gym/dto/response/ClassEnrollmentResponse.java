package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.ClassEnrollment;

public record ClassEnrollmentResponse(
    UUID id,
    UUID classId,
    UUID userId,
    String userName,
    LocalDateTime enrolledAt,
    Boolean attended,
    LocalDateTime cancelledAt
) {
    public static ClassEnrollmentResponse from(ClassEnrollment enrollment) {
        return new ClassEnrollmentResponse(
            enrollment.getId(),
            enrollment.getGymClass().getId(),
            enrollment.getUser().getId(),
            enrollment.getUser().getName(),
            enrollment.getEnrolledAt(),
            enrollment.getAttended(),
            enrollment.getCancelledAt()
        );
    }
}
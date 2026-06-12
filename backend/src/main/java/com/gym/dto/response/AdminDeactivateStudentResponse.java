package com.gym.dto.response;

import com.gym.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminDeactivateStudentResponse(
    UUID userId,
    String email,
    String name,
    Boolean isActive,
    LocalDateTime deactivatedAt,
    UUID deactivatedBy,
    String deactivationReason
) {
    public static AdminDeactivateStudentResponse from(User user) {
        return new AdminDeactivateStudentResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getIsActive(),
            user.getDeactivatedAt(),
            user.getDeactivatedBy() != null ? user.getDeactivatedBy().getId() : null,
            user.getDeactivationReason()
        );
    }
}

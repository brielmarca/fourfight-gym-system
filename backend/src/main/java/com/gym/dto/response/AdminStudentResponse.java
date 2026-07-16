package com.gym.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Membership;
import com.gym.entity.User;

public record AdminStudentResponse(
    UUID id,
    UUID userId,
    String userName,
    String userEmail,
    UUID planId,
    String planName,
    BigDecimal planPrice,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AdminStudentResponse from(User user, Membership membership) {
        if (membership == null) {
            return new AdminStudentResponse(
                user.getId(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                null,
                null,
                null,
                null,
                null,
                "REGISTERED",
                user.getCreatedAt(),
                user.getUpdatedAt()
            );
        }

        return new AdminStudentResponse(
            membership.getId(),
            user.getId(),
            user.getName(),
            user.getEmail(),
            membership.getPlan().getId(),
            membership.getPlan().getName(),
            membership.getPlan().getPrice(),
            membership.getStartDate(),
            membership.getEndDate(),
            membership.getStatus().name(),
            membership.getCreatedAt(),
            membership.getUpdatedAt()
        );
    }
}

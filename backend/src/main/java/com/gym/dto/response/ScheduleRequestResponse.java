package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.ScheduleRequest;

public record ScheduleRequestResponse(
    UUID id,
    UUID userId,
    String userName,
    UUID trainerId,
    String trainerName,
    LocalDateTime preferredAt,
    String notes,
    ScheduleRequest.RequestStatus status,
    LocalDateTime resolvedAt,
    LocalDateTime createdAt
) {
    public static ScheduleRequestResponse from(ScheduleRequest request) {
        return new ScheduleRequestResponse(
            request.getId(),
            request.getUser().getId(),
            request.getUser().getName(),
            request.getTrainer().getId(),
            request.getTrainer().getUser().getName(),
            request.getPreferredAt(),
            request.getNotes(),
            request.getStatus(),
            request.getResolvedAt(),
            request.getCreatedAt()
        );
    }
}
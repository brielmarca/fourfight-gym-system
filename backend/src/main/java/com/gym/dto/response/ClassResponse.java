package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.GymClass;

public record ClassResponse(
    UUID id,
    UUID trainerId,
    String trainerName,
    String name,
    String description,
    Integer capacity,
    Integer enrolledCount,
    LocalDateTime schedule,
    Integer durationMin,
    GymClass.ClassStatus status,
    Boolean isRecurring,
    String recurrenceRule,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ClassResponse from(GymClass gymClass, int enrolledCount) {
        return new ClassResponse(
            gymClass.getId(),
            gymClass.getTrainer().getId(),
            gymClass.getTrainer().getUser().getName(),
            gymClass.getName(),
            gymClass.getDescription(),
            gymClass.getCapacity(),
            enrolledCount,
            gymClass.getSchedule(),
            gymClass.getDurationMin(),
            gymClass.getStatus(),
            gymClass.getIsRecurring(),
            gymClass.getRecurrenceRule(),
            gymClass.getCreatedAt(),
            gymClass.getUpdatedAt()
        );
    }
}
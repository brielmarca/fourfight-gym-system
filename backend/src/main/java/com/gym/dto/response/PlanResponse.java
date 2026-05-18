package com.gym.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.gym.entity.Plan;

public record PlanResponse(
    UUID id,
    String name,
    String description,
    BigDecimal price,
    String currency,
    Integer durationDays,
    Integer maxClasses,
    List<String> features,
    String level,
    String instructor,
    List<String> schedule,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PlanResponse from(Plan plan) {
        List<String> features = plan.getFeatures();
        if (features == null) {
            features = List.of();
        }
        return new PlanResponse(
            plan.getId(),
            plan.getName(),
            plan.getDescription(),
            plan.getPrice(),
            plan.getCurrency(),
            plan.getDurationDays(),
            plan.getMaxClasses(),
            features,
            plan.getLevel(),
            plan.getInstructor(),
            plan.getSchedule(),
            plan.getIsActive(),
            plan.getCreatedAt(),
            plan.getUpdatedAt()
        );
    }
}
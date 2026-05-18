package com.gym.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Trainer;

public record TrainerResponse(
    UUID id,
    UUID userId,
    String userName,
    String userEmail,
    String bio,
    String specialties,
    BigDecimal rating,
    Integer maxClients,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TrainerResponse from(Trainer trainer) {
        return new TrainerResponse(
            trainer.getId(),
            trainer.getUser().getId(),
            trainer.getUser().getName(),
            trainer.getUser().getEmail(),
            trainer.getBio(),
            trainer.getSpecialties(),
            trainer.getRating(),
            trainer.getMaxClients(),
            trainer.getIsActive(),
            trainer.getCreatedAt(),
            trainer.getUpdatedAt()
        );
    }
}
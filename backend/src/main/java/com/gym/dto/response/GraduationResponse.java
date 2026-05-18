package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Graduation;

public record GraduationResponse(
    UUID id,
    String name,
    Integer levelOrder,
    UUID martialArtId,
    String martialArtName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static GraduationResponse from(Graduation graduation) {
        return new GraduationResponse(
            graduation.getId(),
            graduation.getName(),
            graduation.getLevelOrder(),
            graduation.getMartialArt().getId(),
            graduation.getMartialArt().getName(),
            graduation.getCreatedAt(),
            graduation.getUpdatedAt()
        );
    }
}

package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Belt;

public record BeltResponse(
    UUID id,
    String name,
    String colorHex,
    Integer rankOrder,
    LocalDateTime createdAt
) {
    public static BeltResponse from(Belt belt) {
        return new BeltResponse(
            belt.getId(),
            belt.getName(),
            belt.getColorHex(),
            belt.getRankOrder(),
            belt.getCreatedAt()
        );
    }
}
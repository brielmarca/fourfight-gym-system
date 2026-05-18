package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.MartialArt;

public record MartialArtResponse(
    UUID id,
    String name,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static MartialArtResponse from(MartialArt martialArt) {
        return new MartialArtResponse(
            martialArt.getId(),
            martialArt.getName(),
            martialArt.getCreatedAt(),
            martialArt.getUpdatedAt()
        );
    }
}

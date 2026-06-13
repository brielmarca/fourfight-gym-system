package com.gym.dto.response;

import java.util.UUID;

public record AdminGraduationOptionResponse(
    UUID id,
    String name,
    Integer levelOrder,
    String modality,
    UUID martialArtId,
    String martialArtName
) {}

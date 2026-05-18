package com.gym.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.StudentMartialArt;

public record StudentMartialArtResponse(
    UUID id,
    UUID studentId,
    String studentName,
    UUID martialArtId,
    String martialArtName,
    UUID graduationId,
    String graduationName,
    Integer graduationLevelOrder,
    LocalDate startDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static StudentMartialArtResponse from(StudentMartialArt studentMartialArt) {
        return new StudentMartialArtResponse(
            studentMartialArt.getId(),
            studentMartialArt.getStudent().getId(),
            studentMartialArt.getStudent().getName(),
            studentMartialArt.getMartialArt().getId(),
            studentMartialArt.getMartialArt().getName(),
            studentMartialArt.getGraduation().getId(),
            studentMartialArt.getGraduation().getName(),
            studentMartialArt.getGraduation().getLevelOrder(),
            studentMartialArt.getStartDate(),
            studentMartialArt.getCreatedAt(),
            studentMartialArt.getUpdatedAt()
        );
    }
}

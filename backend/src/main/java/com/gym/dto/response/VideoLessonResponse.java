package com.gym.dto.response;

import com.gym.entity.TeachingModality;
import com.gym.entity.VideoLesson;
import java.time.LocalDateTime;
import java.util.UUID;

public record VideoLessonResponse(
    UUID id,
    String title,
    String description,
    TeachingModality modality,
    String videoUrl,
    String embedUrl,
    VideoLesson.VideoProvider provider,
    Integer minimumPlanRank,
    UUID professorId,
    String professorName,
    UUID createdById,
    String createdByName,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static VideoLessonResponse from(VideoLesson lesson) {
        return new VideoLessonResponse(
            lesson.getId(),
            lesson.getTitle(),
            lesson.getDescription(),
            lesson.getModality(),
            lesson.getVideoUrl(),
            lesson.getEmbedUrl(),
            lesson.getProvider(),
            lesson.getMinimumPlanRank(),
            lesson.getProfessor() != null ? lesson.getProfessor().getId() : null,
            lesson.getProfessor() != null ? lesson.getProfessor().getName() : null,
            lesson.getCreatedBy().getId(),
            lesson.getCreatedBy().getName(),
            lesson.getActive(),
            lesson.getCreatedAt(),
            lesson.getUpdatedAt()
        );
    }
}

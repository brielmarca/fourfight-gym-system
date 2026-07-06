package com.gym.dto.response;

import com.gym.entity.StudentProfile;
import java.time.LocalDateTime;
import java.util.UUID;

public record StaffStudentProfileResponse(
    UUID id,
    UUID userId,
    String userName,
    UUID beltId,
    String beltName,
    String beltColorHex,
    String trainingDays,
    String goals,
    String observations,
    Boolean isActive,
    LocalDateTime createdAt
) {
    public static StaffStudentProfileResponse from(StudentProfile profile) {
        return new StaffStudentProfileResponse(
            profile.getId(),
            profile.getUser().getId(),
            profile.getUser().getName(),
            profile.getBelt() != null ? profile.getBelt().getId() : null,
            profile.getBelt() != null ? profile.getBelt().getName() : null,
            profile.getBelt() != null ? profile.getBelt().getColorHex() : null,
            profile.getTrainingDays(),
            profile.getGoals(),
            profile.getObservations(),
            profile.getIsActive(),
            profile.getCreatedAt()
        );
    }
}

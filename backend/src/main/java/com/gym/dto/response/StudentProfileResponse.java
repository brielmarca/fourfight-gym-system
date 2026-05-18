package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.StudentProfile;

public record StudentProfileResponse(
    UUID id,
    UUID userId,
    String userName,
    String userEmail,
    UUID beltId,
    String beltName,
    String beltColorHex,
    String trainingDays,
    String emergencyContact,
    String emergencyPhone,
    String medicalNotes,
    String recoveryNotes,
    String goals,
    String observations,
    Boolean isActive,
    LocalDateTime createdAt
) {
    public static StudentProfileResponse from(StudentProfile profile) {
        return new StudentProfileResponse(
            profile.getId(),
            profile.getUser().getId(),
            profile.getUser().getName(),
            profile.getUser().getEmail(),
            profile.getBelt() != null ? profile.getBelt().getId() : null,
            profile.getBelt() != null ? profile.getBelt().getName() : null,
            profile.getBelt() != null ? profile.getBelt().getColorHex() : null,
            profile.getTrainingDays(),
            profile.getEmergencyContact(),
            profile.getEmergencyPhone(),
            profile.getMedicalNotes(),
            profile.getRecoveryNotes(),
            profile.getGoals(),
            profile.getObservations(),
            profile.getIsActive(),
            profile.getCreatedAt()
        );
    }
}
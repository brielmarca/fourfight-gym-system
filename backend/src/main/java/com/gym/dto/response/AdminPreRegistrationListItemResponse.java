package com.gym.dto.response;

import com.gym.entity.PreRegistrationProfile;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record AdminPreRegistrationListItemResponse(
    UUID id,
    UUID userId,
    String name,
    String email,
    String phone,
    Integer age,
    String preferredModality,
    String preferredTrainingTime,
    Set<PreRegistrationProfile.PreferredTrainingDay> preferredTrainingDays,
    String preferredContactMethod,
    LocalDateTime createdAt
) {
    public static AdminPreRegistrationListItemResponse from(PreRegistrationProfile profile) {
        return new AdminPreRegistrationListItemResponse(
            profile.getId(),
            profile.getUser().getId(),
            profile.getUser().getName(),
            profile.getUser().getEmail(),
            profile.getPhone(),
            profile.getAge(),
            profile.getPreferredModality().name(),
            profile.getPreferredTrainingTime().name(),
            profile.getPreferredTrainingDays(),
            profile.getPreferredContactMethod().name(),
            profile.getCreatedAt()
        );
    }
}

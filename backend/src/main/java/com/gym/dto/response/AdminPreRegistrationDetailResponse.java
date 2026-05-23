package com.gym.dto.response;

import com.gym.entity.PreRegistrationProfile;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record AdminPreRegistrationDetailResponse(
    UUID id,
    UUID userId,
    String name,
    String email,
    String phone,
    Integer age,
    LocalDateTime accountCreatedAt,
    String parishOrArea,
    Boolean hasMartialArtsExperience,
    String martialArtsExperienceDetails,
    String trainingGoal,
    String preferredModality,
    String preferredModalityOther,
    String preferredTrainingTime,
    String preferredTrainingTimeOther,
    Set<PreRegistrationProfile.PreferredTrainingDay> preferredTrainingDays,
    Boolean valuesMartialArtsPhilosophy,
    String preferredContactMethod,
    String preferredContactMethodOther,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AdminPreRegistrationDetailResponse from(PreRegistrationProfile profile) {
        return new AdminPreRegistrationDetailResponse(
            profile.getId(),
            profile.getUser().getId(),
            profile.getUser().getName(),
            profile.getUser().getEmail(),
            profile.getPhone(),
            profile.getAge(),
            profile.getUser().getCreatedAt(),
            profile.getParishOrArea(),
            profile.getHasMartialArtsExperience(),
            profile.getMartialArtsExperienceDetails(),
            profile.getTrainingGoal(),
            profile.getPreferredModality().name(),
            profile.getPreferredModalityOther(),
            profile.getPreferredTrainingTime().name(),
            profile.getPreferredTrainingTimeOther(),
            profile.getPreferredTrainingDays(),
            profile.getValuesMartialArtsPhilosophy(),
            profile.getPreferredContactMethod().name(),
            profile.getPreferredContactMethodOther(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}

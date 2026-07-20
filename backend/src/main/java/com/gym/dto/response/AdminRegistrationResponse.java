package com.gym.dto.response;

import com.gym.entity.PreRegistrationLead;
import com.gym.entity.PreRegistrationProfile;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

public record AdminRegistrationResponse(
    UUID id,
    UUID userId,
    UUID leadId,
    String source,
    String fullName,
    String email,
    String phone,
    Integer age,
    String parish,
    Boolean hasMartialArtsExperience,
    String martialArtsExperienceDetails,
    String trainingGoal,
    String preferredModalities,
    String preferredModalityOther,
    String preferredTrainingTimes,
    String preferredTrainingTimeOther,
    String preferredTrainingDays,
    Boolean philosophyImportant,
    String preferredContactMethod,
    String preferredContactMethodOther,
    LocalDateTime submittedAt,
    String status,
    String notes,
    LocalDateTime accountCreatedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AdminRegistrationResponse from(PreRegistrationLead lead) {
        return new AdminRegistrationResponse(
            lead.getId(),
            null,
            lead.getId(),
            "CSV",
            lead.getFullName(),
            null,
            lead.getPhone(),
            lead.getAge(),
            lead.getParish(),
            lead.getHasMartialArtsExperience(),
            lead.getMartialArtsExperienceDetails(),
            lead.getTrainingGoal(),
            lead.getPreferredModalities(),
            null,
            lead.getPreferredTrainingTimes(),
            null,
            lead.getPreferredTrainingDays(),
            lead.getPhilosophyImportant(),
            lead.getPreferredContactMethod(),
            null,
            lead.getSubmittedAt(),
            lead.getStatus(),
            lead.getNotes(),
            null,
            lead.getCreatedAt(),
            lead.getUpdatedAt()
        );
    }

    public static AdminRegistrationResponse from(PreRegistrationProfile profile) {
        return new AdminRegistrationResponse(
            profile.getId(),
            profile.getUser().getId(),
            null,
            "SITE",
            profile.getUser().getName(),
            profile.getUser().getEmail(),
            profile.getPhone(),
            profile.getAge(),
            profile.getParishOrArea(),
            profile.getHasMartialArtsExperience(),
            profile.getMartialArtsExperienceDetails(),
            profile.getTrainingGoal(),
            profile.getPreferredModality().name(),
            profile.getPreferredModalityOther(),
            profile.getPreferredTrainingTime().name(),
            profile.getPreferredTrainingTimeOther(),
            profile.getPreferredTrainingDays().stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(Enum::name)
                .collect(Collectors.joining(",")),
            profile.getValuesMartialArtsPhilosophy(),
            profile.getPreferredContactMethod().name(),
            profile.getPreferredContactMethodOther(),
            profile.getCreatedAt(),
            "REGISTERED",
            null,
            profile.getUser().getCreatedAt(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}

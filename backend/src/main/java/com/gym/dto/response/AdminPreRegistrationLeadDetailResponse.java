package com.gym.dto.response;

import com.gym.entity.PreRegistrationLead;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminPreRegistrationLeadDetailResponse(
    UUID id,
    LocalDateTime submittedAt,
    String fullName,
    Integer age,
    String phone,
    String parish,
    Boolean hasMartialArtsExperience,
    String martialArtsExperienceDetails,
    String trainingGoal,
    String preferredModalities,
    String preferredTrainingTimes,
    String preferredTrainingDays,
    Boolean philosophyImportant,
    String preferredContactMethod,
    String source,
    String status,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AdminPreRegistrationLeadDetailResponse from(PreRegistrationLead lead) {
        return new AdminPreRegistrationLeadDetailResponse(
            lead.getId(),
            lead.getSubmittedAt(),
            lead.getFullName(),
            lead.getAge(),
            lead.getPhone(),
            lead.getParish(),
            lead.getHasMartialArtsExperience(),
            lead.getMartialArtsExperienceDetails(),
            lead.getTrainingGoal(),
            lead.getPreferredModalities(),
            lead.getPreferredTrainingTimes(),
            lead.getPreferredTrainingDays(),
            lead.getPhilosophyImportant(),
            lead.getPreferredContactMethod(),
            lead.getSource(),
            lead.getStatus(),
            lead.getNotes(),
            lead.getCreatedAt(),
            lead.getUpdatedAt()
        );
    }
}

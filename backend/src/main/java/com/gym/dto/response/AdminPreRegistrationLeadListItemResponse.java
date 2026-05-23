package com.gym.dto.response;

import com.gym.entity.PreRegistrationLead;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminPreRegistrationLeadListItemResponse(
    UUID id,
    String fullName,
    String phone,
    Integer age,
    String parish,
    String preferredModalities,
    String preferredTrainingTimes,
    String preferredTrainingDays,
    String preferredContactMethod,
    LocalDateTime submittedAt,
    String status
) {
    public static AdminPreRegistrationLeadListItemResponse from(PreRegistrationLead lead) {
        return new AdminPreRegistrationLeadListItemResponse(
            lead.getId(),
            lead.getFullName(),
            lead.getPhone(),
            lead.getAge(),
            lead.getParish(),
            lead.getPreferredModalities(),
            lead.getPreferredTrainingTimes(),
            lead.getPreferredTrainingDays(),
            lead.getPreferredContactMethod(),
            lead.getSubmittedAt(),
            lead.getStatus()
        );
    }
}

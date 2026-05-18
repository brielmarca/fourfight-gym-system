package com.gym.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateStudentProfileRequest(
    @NotNull UUID userId,
    UUID beltId,
    String trainingDays,
    String emergencyContact,
    String emergencyPhone,
    String medicalNotes,
    String recoveryNotes,
    String goals,
    String observations
) {}
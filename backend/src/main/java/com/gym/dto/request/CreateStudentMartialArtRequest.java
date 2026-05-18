package com.gym.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateStudentMartialArtRequest(
    @NotNull(message = "Student ID is required")
    UUID studentId,

    @NotNull(message = "Martial art ID is required")
    UUID martialArtId,

    @NotNull(message = "Graduation ID is required")
    UUID graduationId,

    @NotNull(message = "Start date is required")
    LocalDate startDate
) {}

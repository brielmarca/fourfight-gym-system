package com.gym.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateAttendanceRequest(
    @NotNull UUID studentId,
    @NotNull LocalDate date,
    @NotNull Boolean present,
    UUID classId,
    String notes
) {}
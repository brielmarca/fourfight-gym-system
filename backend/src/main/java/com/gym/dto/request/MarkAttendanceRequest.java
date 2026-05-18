package com.gym.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record MarkAttendanceRequest(
    @NotNull(message = "Student ID is required")
    UUID studentId,
    
    LocalDate attendanceDate
) {}

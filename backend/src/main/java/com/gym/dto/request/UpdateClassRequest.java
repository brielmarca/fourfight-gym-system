package com.gym.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import com.gym.entity.GymClass;

public record UpdateClassRequest(
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name,

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 100, message = "Capacity must not exceed 100")
    Integer capacity,

    LocalDateTime schedule,

    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 240, message = "Duration must not exceed 240 minutes")
    Integer durationMin,

    GymClass.ClassStatus status,

    Boolean isRecurring,

    @Size(max = 100, message = "Recurrence rule must not exceed 100 characters")
    String recurrenceRule
) {}
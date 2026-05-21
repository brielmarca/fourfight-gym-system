package com.gym.dto.request;

import com.gym.entity.ClassSchedule.ClassLevel;
import com.gym.entity.ClassSchedule.DayOfWeek;
import com.gym.entity.ClassSchedule.Modality;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record UpdateClassScheduleRequest(
    @Size(max = 255, message = "Class name must not exceed 255 characters")
    String title,
    Modality modality,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    @Size(max = 255, message = "Instructor name must not exceed 255 characters")
    String instructorName,
    ClassLevel level,
    @Size(max = 255, message = "Location must not exceed 255 characters")
    String location,
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 500, message = "Capacity must not exceed 500")
    Integer capacity,
    Boolean active,
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    String notes
) {}

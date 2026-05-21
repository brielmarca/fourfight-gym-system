package com.gym.dto.response;

import com.gym.entity.ClassSchedule;
import com.gym.entity.ClassSchedule.ClassLevel;
import com.gym.entity.ClassSchedule.DayOfWeek;
import com.gym.entity.ClassSchedule.Modality;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ClassScheduleResponse(
    UUID id,
    String title,
    Modality modality,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    String instructorName,
    ClassLevel level,
    String location,
    Integer capacity,
    Boolean active,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ClassScheduleResponse from(ClassSchedule schedule) {
        return new ClassScheduleResponse(
            schedule.getId(),
            schedule.getTitle(),
            schedule.getModality(),
            schedule.getDayOfWeek(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            schedule.getInstructorName(),
            schedule.getLevel(),
            schedule.getLocation(),
            schedule.getCapacity(),
            schedule.getActive(),
            schedule.getNotes(),
            schedule.getCreatedAt(),
            schedule.getUpdatedAt()
        );
    }
}

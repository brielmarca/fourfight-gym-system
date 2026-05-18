package com.gym.dto.response;

import java.time.LocalDate;
import java.util.UUID;
import com.gym.entity.TrainingAttendance;

public record TrainingAttendanceResponse(
    UUID id,
    UUID studentId,
    String studentName,
    LocalDate date,
    Boolean present,
    UUID classId,
    String notes,
    UUID recordedBy
) {
    public static TrainingAttendanceResponse from(TrainingAttendance attendance) {
        return new TrainingAttendanceResponse(
            attendance.getId(),
            attendance.getStudent().getId(),
            attendance.getStudent().getName(),
            attendance.getDate(),
            attendance.getPresent(),
            attendance.getClassId(),
            attendance.getNotes(),
            attendance.getRecordedBy()
        );
    }
}
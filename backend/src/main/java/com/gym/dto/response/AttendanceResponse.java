package com.gym.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Attendance;

public record AttendanceResponse(
    UUID id,
    UUID studentId,
    String studentName,
    LocalDate attendanceDate,
    LocalDateTime createdAt
) {
    public static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(
            attendance.getId(),
            attendance.getStudent().getId(),
            attendance.getStudent().getName(),
            attendance.getAttendanceDate(),
            attendance.getCreatedAt()
        );
    }
}

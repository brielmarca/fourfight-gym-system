package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.TrialBooking;

public record TrialBookingResponse(
    UUID id,
    String name,
    String email,
    String phone,
    String program,
    TrialBooking.BookingStatus status,
    UUID processedBy,
    String processedByName,
    LocalDateTime processedAt,
    LocalDateTime createdAt
) {
    public static TrialBookingResponse from(TrialBooking booking) {
        return new TrialBookingResponse(
            booking.getId(),
            booking.getName(),
            booking.getEmail(),
            booking.getPhone(),
            booking.getProgram(),
            booking.getStatus(),
            booking.getProcessedBy() != null ? booking.getProcessedBy().getId() : null,
            booking.getProcessedBy() != null ? booking.getProcessedBy().getName() : null,
            booking.getProcessedAt(),
            booking.getCreatedAt()
        );
    }
}
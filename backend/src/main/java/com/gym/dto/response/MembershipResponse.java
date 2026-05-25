package com.gym.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Membership;

public record MembershipResponse(
    UUID id,
    UUID userId,
    String userName,
    String userEmail,
    UUID planId,
    String planName,
    LocalDate startDate,
    LocalDate endDate,
    Membership.MembershipStatus status,
    Boolean autoRenew,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static MembershipResponse from(Membership membership) {
        return new MembershipResponse(
            membership.getId(),
            membership.getUser().getId(),
            membership.getUser().getName(),
            membership.getUser().getEmail(),
            membership.getPlan().getId(),
            membership.getPlan().getName(),
            membership.getStartDate(),
            membership.getEndDate(),
            membership.getStatus(),
            membership.getAutoRenew(),
            membership.getCreatedAt(),
            membership.getUpdatedAt()
        );
    }
}

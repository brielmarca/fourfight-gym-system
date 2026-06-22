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
    PlanResponse plan,
    LocalDate startDate,
    LocalDate endDate,
    Membership.MembershipStatus status,
    Boolean autoRenew,
    Boolean stripePayment,
    LocalDate currentPeriodEnd,
    Boolean cancelAtPeriodEnd,
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
            PlanResponse.from(membership.getPlan()),
            membership.getStartDate(),
            membership.getEndDate(),
            membership.getStatus(),
            membership.getAutoRenew(),
            membership.getStripeSubscriptionId() != null && !membership.getStripeSubscriptionId().isBlank(),
            membership.getCurrentPeriodEnd(),
            membership.getCancelAtPeriodEnd(),
            membership.getCreatedAt(),
            membership.getUpdatedAt()
        );
    }
}

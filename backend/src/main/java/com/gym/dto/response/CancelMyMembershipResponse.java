package com.gym.dto.response;

import com.gym.entity.Membership;
import java.time.LocalDate;

public record CancelMyMembershipResponse(
    Membership.MembershipStatus status,
    boolean cancellationScheduled,
    LocalDate accessUntil,
    String message
) {}

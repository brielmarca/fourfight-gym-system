package com.gym.dto.response;

import com.gym.entity.Membership;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReceptionRequestResponse(
    UUID membershipId,
    UUID userId,
    String userName,
    String userEmail,
    UUID planId,
    String planName,
    BigDecimal planPrice,
    Membership.MembershipStatus status,
    String paymentMethod,
    String paymentStatus,
    String message,
    LocalDateTime requestedAt
) {}

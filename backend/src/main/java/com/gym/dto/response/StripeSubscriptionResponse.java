package com.gym.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StripeSubscriptionResponse(
    UUID id,
    String planName,
    BigDecimal planPrice,
    String status,
    LocalDate currentPeriodStart,
    LocalDate currentPeriodEnd,
    boolean cancelAtPeriodEnd,
    String stripeSubscriptionId
) {}

package com.gym.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Payment;

public record PaymentResponse(
    UUID id,
    UUID userId,
    String userName,
    UUID membershipId,
    BigDecimal amount,
    String currency,
    Payment.PaymentMethod method,
    Payment.PaymentStatus status,
    String gatewayRef,
    LocalDateTime paidAt,
    LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getUser().getId(),
            payment.getUser().getName(),
            payment.getMembership() != null ? payment.getMembership().getId() : null,
            payment.getAmount(),
            payment.getCurrency(),
            payment.getMethod(),
            payment.getStatus(),
            payment.getGatewayRef(),
            payment.getPaidAt(),
            payment.getCreatedAt()
        );
    }
}
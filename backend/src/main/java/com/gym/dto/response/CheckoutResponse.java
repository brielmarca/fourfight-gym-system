package com.gym.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {
    private UUID id;
    private String name;
    private String email;
    private String planName;
    private BigDecimal planPrice;
    private String paymentMethod;
    private Payment.PaymentStatus paymentStatus;
    private String message;
    private UUID userId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime createdAt;
}

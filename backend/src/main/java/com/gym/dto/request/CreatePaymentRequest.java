package com.gym.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import com.gym.entity.Payment;

public record CreatePaymentRequest(
    @NotNull(message = "Membership ID is required")
    UUID membershipId,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,
    
    @Size(max = 3, message = "Currency must not exceed 3 characters")
    String currency,
    
    Payment.PaymentMethod method
) {}
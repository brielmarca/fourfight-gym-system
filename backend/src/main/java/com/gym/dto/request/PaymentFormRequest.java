package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PaymentFormRequest(
    // MBWay
    String phoneNumber,
    
    // Card
    String cardHolderName,
    String cardNumber,
    String expirationDate,
    String cvv,
    
    // Common
    @NotBlank(message = "Payment ID is required")
    String paymentId
) {}

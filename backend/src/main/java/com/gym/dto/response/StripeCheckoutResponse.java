package com.gym.dto.response;

public record StripeCheckoutResponse(
    String sessionId,
    String checkoutUrl
) {}

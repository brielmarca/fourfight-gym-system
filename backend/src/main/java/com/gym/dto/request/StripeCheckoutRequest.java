package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StripeCheckoutRequest(
    @NotBlank String planId
) {}

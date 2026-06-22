package com.gym.dto.request;

import jakarta.validation.constraints.Size;

public record CancelMyMembershipRequest(
    @Size(max = 500)
    String reason
) {}

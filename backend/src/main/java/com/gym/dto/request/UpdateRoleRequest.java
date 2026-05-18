package com.gym.dto.request;

import jakarta.validation.constraints.NotNull;
import com.gym.entity.User;

public record UpdateRoleRequest(
    @NotNull(message = "Role is required")
    User.Role role
) {}
package com.gym.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateStudentRequest(
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name,

    @Email(message = "Email must be valid")
    String email,

    UUID planId,

    Boolean isActive
) {}

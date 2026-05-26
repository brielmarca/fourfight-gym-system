package com.gym.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookTrialRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @Pattern(regexp = "^$|^\\+?[0-9()\\-\\s]{7,20}$", message = "Invalid phone format")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    String phone
) {}

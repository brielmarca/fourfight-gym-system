package com.gym.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record UpdatePlanRequest(
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name,

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "99999.99", message = "Price must not exceed 99999.99")
    BigDecimal price,

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration must not exceed 365 days")
    Integer durationDays,

    @Min(value = 0, message = "Max classes must be at least 0")
    Integer maxClasses,

    List<String> features,

    String level,

    String instructor,

    List<String> schedule,

    Boolean isActive
) {}
package com.gym.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record RegisterRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter")
    @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one digit")
    String password,

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9()\\-\\s]{7,20}$", message = "Invalid phone format")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    String phone,

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,

    @NotNull(message = "Age is required")
    @Min(value = 3, message = "Age must be at least 3")
    @Max(value = 100, message = "Age must be at most 100")
    Integer age,

    @NotBlank(message = "Parish or area is required")
    @Size(max = 255, message = "Parish or area must not exceed 255 characters")
    String parishOrArea,

    @NotNull(message = "Martial arts experience answer is required")
    Boolean hasMartialArtsExperience,

    @Size(max = 1500, message = "Martial arts experience details must not exceed 1500 characters")
    String martialArtsExperienceDetails,

    @NotBlank(message = "Seleciona o teu objetivo de treino.")
    @Size(max = 2000, message = "Training goal must not exceed 2000 characters")
    String trainingGoal,

    @NotNull(message = "Preferred modality is required")
    PreferredModality preferredModality,

    @Size(max = 255, message = "Preferred modality other must not exceed 255 characters")
    String preferredModalityOther,

    @NotNull(message = "Preferred training time is required")
    PreferredTrainingTime preferredTrainingTime,

    @Size(max = 255, message = "Preferred training time other must not exceed 255 characters")
    String preferredTrainingTimeOther,

    @NotEmpty(message = "Preferred training days must not be empty")
    List<PreferredTrainingDay> preferredTrainingDays,

    @NotNull(message = "Martial arts philosophy preference is required")
    Boolean valuesMartialArtsPhilosophy,

    @NotNull(message = "Preferred contact method is required")
    PreferredContactMethod preferredContactMethod,

    @Size(max = 255, message = "Preferred contact method other must not exceed 255 characters")
    String preferredContactMethodOther
) {}

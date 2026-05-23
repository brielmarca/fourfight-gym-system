package com.gym.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pre_registration_leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreRegistrationLead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "age")
    private Integer age;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "parish", length = 255)
    private String parish;

    @Column(name = "has_martial_arts_experience")
    private Boolean hasMartialArtsExperience;

    @Column(name = "martial_arts_experience_details", columnDefinition = "TEXT")
    private String martialArtsExperienceDetails;

    @Column(name = "training_goal", columnDefinition = "TEXT")
    private String trainingGoal;

    @Column(name = "preferred_modalities", columnDefinition = "TEXT")
    private String preferredModalities;

    @Column(name = "preferred_training_times", columnDefinition = "TEXT")
    private String preferredTrainingTimes;

    @Column(name = "preferred_training_days", columnDefinition = "TEXT")
    private String preferredTrainingDays;

    @Column(name = "philosophy_important")
    private Boolean philosophyImportant;

    @Column(name = "preferred_contact_method", length = 100)
    private String preferredContactMethod;

    @Column(name = "source", nullable = false, length = 100)
    private String source;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

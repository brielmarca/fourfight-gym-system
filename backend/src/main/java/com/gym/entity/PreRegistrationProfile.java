package com.gym.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pre_registration_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreRegistrationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "parish_or_area", nullable = false, length = 255)
    private String parishOrArea;

    @Column(name = "has_martial_arts_experience", nullable = false)
    private Boolean hasMartialArtsExperience;

    @Column(name = "martial_arts_experience_details", columnDefinition = "TEXT")
    private String martialArtsExperienceDetails;

    @Column(name = "training_goal", nullable = false, columnDefinition = "TEXT")
    private String trainingGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_modality", nullable = false, length = 40)
    private PreferredModality preferredModality;

    @Column(name = "preferred_modality_other", length = 255)
    private String preferredModalityOther;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_training_time", nullable = false, length = 40)
    private PreferredTrainingTime preferredTrainingTime;

    @Column(name = "preferred_training_time_other", length = 255)
    private String preferredTrainingTimeOther;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pre_registration_profile_days", joinColumns = @JoinColumn(name = "profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "\"day\"", nullable = false, length = 20)
    @Builder.Default
    private Set<PreferredTrainingDay> preferredTrainingDays = new HashSet<>();

    @Column(name = "values_martial_arts_philosophy", nullable = false)
    private Boolean valuesMartialArtsPhilosophy;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method", nullable = false, length = 20)
    private PreferredContactMethod preferredContactMethod;

    @Column(name = "preferred_contact_method_other", length = 255)
    private String preferredContactMethodOther;

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

    public enum PreferredModality {
        KICKBOXING,
        JIU_JITSU,
        CAPOEIRA,
        BOXE,
        MMA,
        JIU_JITSU_KIDS,
        CAPOEIRA_KIDS,
        KICKBOXING_KIDS,
        OTHER
    }

    public enum PreferredTrainingTime {
        MORNING_BEFORE_0830,
        LUNCH_1230,
        AFTERNOON_14_17,
        NIGHT_AFTER_18,
        OTHER
    }

    public enum PreferredTrainingDay {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY
    }

    public enum PreferredContactMethod {
        CALL,
        MESSAGE,
        OTHER
    }
}

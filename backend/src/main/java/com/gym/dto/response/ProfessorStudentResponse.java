package com.gym.dto.response;

import com.gym.entity.Membership;
import com.gym.entity.TeachingModality;
import java.time.LocalDateTime;

public record ProfessorStudentResponse(
    String studentName,
    String studentEmail,
    TeachingModality modality,
    Membership.MembershipStatus membershipStatus,
    String planName,
    String notes,
    LocalDateTime assignedAt
) {}

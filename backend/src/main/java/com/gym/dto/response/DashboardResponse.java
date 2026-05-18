package com.gym.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record DashboardResponse(
    long totalStudents,
    long activeStudents,
    long todayAttendance,
    long newMembersMTD,
    java.math.BigDecimal avgValue,
    java.util.List<PlanCount> studentsByPlan,
    java.util.List<RecentPromotion> recentPromotions
) {
    public record PlanCount(
        UUID planId,
        String planName,
        long count
    ) {}

    public record RecentPromotion(
        UUID studentId,
        String studentName,
        String martialArtName,
        String fromGraduation,
        String toGraduation,
        LocalDate date
    ) {}
}

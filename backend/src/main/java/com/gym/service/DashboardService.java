package com.gym.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.response.DashboardResponse;
import com.gym.repository.AttendanceRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.StudentMartialArtRepository;
import com.gym.repository.StudentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final PlanRepository planRepository;
    private final StudentMartialArtRepository studentMartialArtRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long totalStudents = studentRepository.count();
        long activeStudents = studentRepository.countByIsActiveTrue();
        long todayAttendance = attendanceRepository.countByAttendanceDate(LocalDate.now());

        List<DashboardResponse.PlanCount> studentsByPlan = planRepository.findAll().stream()
                .map(plan -> {
                    long count = studentRepository.countByPlanIdAndIsActiveTrue(plan.getId());
                    return new DashboardResponse.PlanCount(plan.getId(), plan.getName(), count);
                })
                .collect(Collectors.toList());

        List<DashboardResponse.RecentPromotion> recentPromotions = studentMartialArtRepository
                .findTop10ByOrderByCreatedAtDesc().stream()
                .map(sma -> new DashboardResponse.RecentPromotion(
                        sma.getStudent().getId(),
                        sma.getStudent().getName(),
                        sma.getMartialArt().getName(),
                        "N/A",  // Would need graduation history to show from->to
                        sma.getGraduation().getName(),
                        sma.getStartDate()
                ))
                .collect(Collectors.toList());

        return new DashboardResponse(
                totalStudents,
                activeStudents,
                todayAttendance,
                0L,
                java.math.BigDecimal.ZERO,
                studentsByPlan,
                recentPromotions
        );
    }
}

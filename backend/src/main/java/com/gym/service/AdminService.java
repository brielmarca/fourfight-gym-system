package com.gym.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.gym.dto.response.AdminPreRegistrationDetailResponse;
import com.gym.dto.response.AdminPreRegistrationListItemResponse;
import com.gym.dto.response.AuditLogResponse;
import com.gym.dto.response.DashboardResponse;
import com.gym.dto.response.RevenueReportResponse.MonthlyRevenue;
import com.gym.dto.response.RevenueReportResponse;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.AuditLogRepository;
import com.gym.repository.PreRegistrationProfileRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MembershipService membershipService;
    private final UserService userService;
    private final TrainerService trainerService;
    private final ClassService classService;
    private final PaymentService paymentService;
    private final AuditLogRepository auditLogRepository;
    private final PreRegistrationProfileRepository preRegistrationProfileRepository;

    public DashboardResponse getDashboard() {
        long activeMembers = membershipService.countActive();
        long newMembersMTD = userService.countActiveClients();
        var revenueMTD = paymentService.getRevenueMTD() != null ? paymentService.getRevenueMTD() : java.math.BigDecimal.ZERO;
        
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        long classesToday = classService.getTodayClassCount(startOfDay, endOfDay);

        java.math.BigDecimal avgValue = java.math.BigDecimal.ZERO;
        if (activeMembers > 0 && revenueMTD.compareTo(java.math.BigDecimal.ZERO) > 0) {
            avgValue = revenueMTD.divide(java.math.BigDecimal.valueOf(activeMembers), 2, java.math.RoundingMode.HALF_UP);
        }

        return new DashboardResponse(
            activeMembers,
            0L,
            classesToday,
            newMembersMTD,
            avgValue,
            List.of(),
            List.of()
        );
    }

    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllOrderByCreatedAtDesc(pageable).map(AuditLogResponse::from);
    }

    public RevenueReportResponse getRevenueReport() {
        java.math.BigDecimal total = paymentService.getRevenueMTD();
        return new RevenueReportResponse(
            List.of(new RevenueReportResponse.MonthlyRevenue(LocalDateTime.now().getMonth().name(), total != null ? total : java.math.BigDecimal.ZERO)),
            total != null ? total : java.math.BigDecimal.ZERO,
            total != null ? total : java.math.BigDecimal.ZERO
        );
    }

    public Page<AdminPreRegistrationListItemResponse> getPreRegistrations(Pageable pageable) {
        return preRegistrationProfileRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(AdminPreRegistrationListItemResponse::from);
    }

    public AdminPreRegistrationDetailResponse getPreRegistrationById(UUID id) {
        return preRegistrationProfileRepository.findById(id)
            .map(AdminPreRegistrationDetailResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("PreRegistrationProfile", id));
    }
}

package com.gym.controller;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.gym.dto.response.AdminPreRegistrationLeadDetailResponse;
import com.gym.dto.response.AdminPreRegistrationLeadListItemResponse;
import com.gym.dto.response.AuditLogResponse;
import com.gym.dto.response.DashboardResponse;
import com.gym.dto.response.PreRegistrationLeadImportResponse;
import com.gym.dto.response.RevenueReportResponse;
import com.gym.service.AdminService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAuditLogs(pageable));
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<RevenueReportResponse> getRevenueReport() {
        return ResponseEntity.ok(adminService.getRevenueReport());
    }

    @GetMapping("/pre-registrations")
    public ResponseEntity<Page<AdminPreRegistrationLeadListItemResponse>> getPreRegistrations(
        @PageableDefault(size = 50) Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getPreRegistrations(pageable));
    }

    @GetMapping("/pre-registrations/{id}")
    public ResponseEntity<AdminPreRegistrationLeadDetailResponse> getPreRegistrationById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getPreRegistrationById(id));
    }

    @PostMapping(value = "/pre-registrations/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PreRegistrationLeadImportResponse> importPreRegistrations(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminService.importPreRegistrationsCsv(file));
    }
}

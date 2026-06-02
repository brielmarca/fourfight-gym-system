package com.gym.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.gym.dto.request.AdminUpdateStudentGraduationRequest;
import com.gym.dto.request.AdminCreateProfessorAssignmentRequest;
import com.gym.dto.request.AdminCreateProfessorRequest;
import com.gym.dto.request.AdminUpdateProfessorModalitiesRequest;
import com.gym.dto.response.AdminPreRegistrationLeadDetailResponse;
import com.gym.dto.response.AdminPreRegistrationLeadListItemResponse;
import com.gym.dto.response.AdminProfessorAssignmentResponse;
import com.gym.dto.response.AdminProfessorResponse;
import com.gym.dto.response.AdminStudentResponse;
import com.gym.dto.response.AdminStudentGraduationResponse;
import com.gym.dto.response.AuditLogResponse;
import com.gym.dto.response.DashboardResponse;
import com.gym.dto.response.PreRegistrationLeadImportResponse;
import com.gym.dto.response.RevenueReportResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.AdminGraduationService;
import com.gym.service.AdminService;
import com.gym.service.ProfessorManagementService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminGraduationService adminGraduationService;
    private final ProfessorManagementService professorManagementService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAuditLogs(pageable));
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<AdminStudentResponse>> getStudents(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(adminService.getStudents(pageable));
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

    @PatchMapping("/pre-registrations/{id}/accept")
    public ResponseEntity<AdminPreRegistrationLeadDetailResponse> acceptPreRegistration(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.acceptPreRegistration(id));
    }

    @PatchMapping("/pre-registrations/{id}/archive")
    public ResponseEntity<AdminPreRegistrationLeadDetailResponse> archivePreRegistration(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.archivePreRegistration(id));
    }

    @PostMapping(value = "/pre-registrations/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PreRegistrationLeadImportResponse> importPreRegistrations(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminService.importPreRegistrationsCsv(file));
    }

    @GetMapping("/graduations")
    public ResponseEntity<List<AdminStudentGraduationResponse>> getAdminGraduations() {
        return ResponseEntity.ok(adminGraduationService.listAdminGraduations());
    }

    @PutMapping("/graduations")
    public ResponseEntity<AdminStudentGraduationResponse> updateGraduation(
        @Valid @RequestBody AdminUpdateStudentGraduationRequest request
    ) {
        return ResponseEntity.ok(adminGraduationService.updateStudentGraduation(request));
    }

    @GetMapping("/professors")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AdminProfessorResponse>> getProfessors() {
        return ResponseEntity.ok(professorManagementService.listProfessors());
    }

    @PostMapping("/professors")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AdminProfessorResponse> createProfessor(@Valid @RequestBody AdminCreateProfessorRequest request) {
        return ResponseEntity.ok(professorManagementService.promoteProfessor(request));
    }

    @PutMapping("/professors/{professorId}/modalities")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AdminProfessorResponse> updateProfessorModalities(
        @PathVariable UUID professorId,
        @Valid @RequestBody AdminUpdateProfessorModalitiesRequest request
    ) {
        return ResponseEntity.ok(professorManagementService.updateProfessorModalities(professorId, request));
    }

    @GetMapping("/professor-assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AdminProfessorAssignmentResponse>> getProfessorAssignments() {
        return ResponseEntity.ok(professorManagementService.listAssignments());
    }

    @PostMapping("/professor-assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AdminProfessorAssignmentResponse> createProfessorAssignment(
        @Valid @RequestBody AdminCreateProfessorAssignmentRequest request,
        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(professorManagementService.createAssignment(request, principal.id()));
    }

    @PatchMapping("/professor-assignments/{assignmentId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AdminProfessorAssignmentResponse> deactivateProfessorAssignment(@PathVariable UUID assignmentId) {
        return ResponseEntity.ok(professorManagementService.deactivateAssignment(assignmentId));
    }
}

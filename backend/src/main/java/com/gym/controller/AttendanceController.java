package com.gym.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.MarkAttendanceRequest;
import com.gym.dto.response.AttendanceResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.AttendanceService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/my")
    public ResponseEntity<List<AttendanceResponse>> getMyAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(attendanceService.getByStudent(principal.id(), startDate, endDate));
    }

    @GetMapping("/my/monthly")
    public ResponseEntity<Long> getMyMonthlyAttendance(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(attendanceService.getMonthlyAttendance(principal.id()));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER')")
    public ResponseEntity<List<AttendanceResponse>> getStudentAttendance(
            @PathVariable UUID studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getByStudent(studentId, startDate, endDate));
    }

    @GetMapping("/date")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER')")
    public ResponseEntity<List<AttendanceResponse>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByDate(date));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER')")
    public ResponseEntity<AttendanceResponse> create(
            @Valid @RequestBody MarkAttendanceRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(attendanceService.create(request, principal.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER')")
    public ResponseEntity<AttendanceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody MarkAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.update(id, request));
    }
}

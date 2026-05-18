package com.gym.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.CreateScheduleRequestRequest;
import com.gym.dto.response.ScheduleRequestResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.ScheduleRequestService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/schedule-requests")
@RequiredArgsConstructor
public class ScheduleRequestController {

    private final ScheduleRequestService scheduleRequestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER')")
    public ResponseEntity<Page<ScheduleRequestResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(scheduleRequestService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleRequestResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        ScheduleRequestResponse response = scheduleRequestService.getById(id);
        // IDOR fix: check ownership or admin role
        if (!response.userId().equals(principal.id()) && !"ADMIN".equals(principal.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ScheduleRequestResponse> create(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody CreateScheduleRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleRequestService.create(principal.id(), request));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER')")
    public ResponseEntity<ScheduleRequestResponse> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(scheduleRequestService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER')")
    public ResponseEntity<ScheduleRequestResponse> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(scheduleRequestService.reject(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        // IDOR fix: check ownership or admin role
        ScheduleRequestResponse response = scheduleRequestService.getById(id);
        if (!response.userId().equals(principal.id()) && !"ADMIN".equals(principal.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        scheduleRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
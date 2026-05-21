package com.gym.controller;

import com.gym.dto.request.CreateClassScheduleRequest;
import com.gym.dto.request.UpdateClassScheduleRequest;
import com.gym.dto.response.ClassScheduleResponse;
import com.gym.service.ClassScheduleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ClassScheduleService classScheduleService;

    @GetMapping("/api/schedule")
    public ResponseEntity<List<ClassScheduleResponse>> getSchedule() {
        return ResponseEntity.ok(classScheduleService.getActiveSchedule());
    }

    @GetMapping("/api/admin/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClassScheduleResponse>> getAdminSchedule() {
        return ResponseEntity.ok(classScheduleService.getAllForAdmin());
    }

    @PostMapping("/api/admin/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassScheduleResponse> createSchedule(@Valid @RequestBody CreateClassScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classScheduleService.create(request));
    }

    @PutMapping("/api/admin/schedule/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassScheduleResponse> updateSchedule(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateClassScheduleRequest request
    ) {
        return ResponseEntity.ok(classScheduleService.update(id, request));
    }

    @PatchMapping("/api/admin/schedule/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassScheduleResponse> deactivateSchedule(@PathVariable UUID id) {
        return ResponseEntity.ok(classScheduleService.deactivate(id));
    }
}

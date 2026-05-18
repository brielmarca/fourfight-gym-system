package com.gym.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.CreateStudentProfileRequest;
import com.gym.dto.request.UpdateBeltRequest;
import com.gym.dto.response.StudentProfileResponse;
import com.gym.entity.User;
import com.gym.service.StudentProfileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/student-profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping("/me")
    public ResponseEntity<StudentProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(studentProfileService.getByUserId(user.getId()));
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER')")
    public ResponseEntity<StudentProfileResponse> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(studentProfileService.getByUserId(userId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<StudentProfileResponse> create(@Valid @RequestBody CreateStudentProfileRequest request) {
        return ResponseEntity.ok(studentProfileService.create(request));
    }

    @PutMapping("/me")
    public ResponseEntity<StudentProfileResponse> updateMyProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateStudentProfileRequest request) {
        return ResponseEntity.ok(studentProfileService.update(user.getId(), request));
    }

    @PutMapping("/by-user/{userId}/belt")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentProfileResponse> updateBelt(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateBeltRequest request) {
        return ResponseEntity.ok(studentProfileService.updateBelt(userId, request.beltId()));
    }
}

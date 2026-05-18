package com.gym.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.CreateClassRequest;
import com.gym.dto.request.UpdateClassRequest;
import com.gym.dto.response.ClassEnrollmentResponse;
import com.gym.dto.response.ClassResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.ClassService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping
    public ResponseEntity<Page<ClassResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(classService.getUpcoming(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(classService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ClassResponse> create(@Valid @RequestBody CreateClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateClassRequest request) {
        return ResponseEntity.ok(classService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        classService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<ClassEnrollmentResponse> enroll(@PathVariable UUID id, @AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.enroll(id, principal.id()));
    }

    @PostMapping("/{id}/unenroll")
    public ResponseEntity<Void> unenroll(@PathVariable UUID id, @AuthenticationPrincipal JwtUserPrincipal principal) {
        classService.unenroll(id, principal.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/roster")
    public ResponseEntity<Page<ClassEnrollmentResponse>> getRoster(@PathVariable UUID id, @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(classService.getRoster(id, pageable));
    }
}
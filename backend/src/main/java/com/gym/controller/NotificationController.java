package com.gym.controller;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.response.NotificationResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.NotificationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getAll(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAll(principal.id(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.getById(principal.id(), id));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(principal.id(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID id) {
        notificationService.delete(principal.id(), id);
        return ResponseEntity.noContent().build();
    }
}

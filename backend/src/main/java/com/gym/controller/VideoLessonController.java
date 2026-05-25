package com.gym.controller;

import com.gym.dto.request.CreateVideoLessonRequest;
import com.gym.dto.request.UpdateVideoLessonRequest;
import com.gym.dto.response.VideoLessonResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.VideoLessonService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/video-lessons")
@RequiredArgsConstructor
public class VideoLessonController {

    private final VideoLessonService videoLessonService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROFESSOR')")
    public ResponseEntity<VideoLessonResponse> create(
        @Valid @RequestBody CreateVideoLessonRequest request,
        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(videoLessonService.create(request, principal.id(), principal.role()));
    }

    @GetMapping("/manage")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROFESSOR')")
    public ResponseEntity<List<VideoLessonResponse>> listManage(
        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(videoLessonService.listManage(principal.id(), principal.role()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROFESSOR')")
    public ResponseEntity<VideoLessonResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateVideoLessonRequest request,
        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(videoLessonService.update(id, request, principal.id(), principal.role()));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROFESSOR')")
    public ResponseEntity<VideoLessonResponse> deactivate(
        @PathVariable UUID id,
        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(videoLessonService.deactivate(id, principal.id(), principal.role()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<List<VideoLessonResponse>> myLessons(
        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(videoLessonService.listMyLessons(principal.id(), principal.role()));
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PROFESSOR', 'CLIENT')")
    public ResponseEntity<VideoLessonResponse> myLessonById(
        @PathVariable UUID id,
        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(videoLessonService.getMyLessonById(id, principal.id(), principal.role()));
    }
}

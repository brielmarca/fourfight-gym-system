package com.gym.controller;

import com.gym.dto.response.ProfessorStudentResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.ProfessorManagementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/professor")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorManagementService professorManagementService;

    @GetMapping("/students")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<List<ProfessorStudentResponse>> getAssignedStudents(
        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(professorManagementService.listProfessorStudents(principal.id()));
    }
}

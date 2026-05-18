package com.gym.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.response.ClassResponse;
import com.gym.service.ClassService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ClassService classService;

    @GetMapping
    public ResponseEntity<Page<ClassResponse>> getSchedule(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(classService.getAll(pageable));
    }
}

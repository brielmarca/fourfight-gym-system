package com.gym.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.response.ManagerUserDirectoryResponse;
import com.gym.dto.response.TrainerResponse;
import com.gym.service.TrainerService;
import com.gym.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final UserService userService;
    private final TrainerService trainerService;

    @GetMapping("/users")
    public ResponseEntity<Page<ManagerUserDirectoryResponse>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.getManagerDirectory(pageable));
    }

    @GetMapping("/trainers")
    public ResponseEntity<Page<TrainerResponse>> getAllTrainers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(trainerService.getAll(pageable));
    }
}

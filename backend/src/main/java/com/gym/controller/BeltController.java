package com.gym.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.CreateBeltRequest;
import com.gym.dto.response.BeltResponse;
import com.gym.service.BeltService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/belts")
@RequiredArgsConstructor
public class BeltController {

    private final BeltService beltService;

    @GetMapping
    public ResponseEntity<List<BeltResponse>> getAll() {
        return ResponseEntity.ok(beltService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<BeltResponse>> getAllActive() {
        return ResponseEntity.ok(beltService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BeltResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(beltService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BeltResponse> create(@Valid @RequestBody CreateBeltRequest request) {
        return ResponseEntity.ok(beltService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BeltResponse> update(@PathVariable UUID id, @Valid @RequestBody CreateBeltRequest request) {
        return ResponseEntity.ok(beltService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        beltService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
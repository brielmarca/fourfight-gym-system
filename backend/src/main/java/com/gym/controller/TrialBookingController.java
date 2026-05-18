package com.gym.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.BookTrialRequest;
import com.gym.dto.response.TrialBookingResponse;
import com.gym.service.TrialBookingService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/book-trial")
@RequiredArgsConstructor
public class TrialBookingController {

    private final TrialBookingService trialBookingService;

    @GetMapping
    public ResponseEntity<Page<TrialBookingResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(trialBookingService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrialBookingResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(trialBookingService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TrialBookingResponse> create(
            @Valid @RequestBody BookTrialRequest request,
            @RequestParam(defaultValue = "JIU-JITSU") String program) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trialBookingService.create(request, program));
    }

    @PatchMapping("/{id}/process")
    public ResponseEntity<TrialBookingResponse> process(@PathVariable UUID id) {
        return ResponseEntity.ok(trialBookingService.process(id, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        trialBookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
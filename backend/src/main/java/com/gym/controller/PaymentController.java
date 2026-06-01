package com.gym.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.CreatePaymentRequest;
import com.gym.dto.response.PaymentResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.PaymentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PaymentResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        if ("ADMIN".equals(principal.role()) || "MANAGER".equals(principal.role())) {
            return ResponseEntity.ok(paymentService.getAllForAdmin(pageable));
        }
        return ResponseEntity.ok(paymentService.getPaymentsForCurrentUser(principal.id(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        PaymentResponse payment = paymentService.getById(id);
        // IDOR fix: check ownership or admin role
        if (!payment.userId().equals(principal.id())
                && !"ADMIN".equals(principal.role())
                && !"MANAGER".equals(principal.role())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(payment);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(request, principal.id(), principal.role()));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PaymentResponse> complete(@PathVariable UUID id, @RequestParam String gatewayRef) {
        return ResponseEntity.ok(paymentService.complete(id, gatewayRef, null));
    }

    @PatchMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> refund(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.refund(id));
    }
}

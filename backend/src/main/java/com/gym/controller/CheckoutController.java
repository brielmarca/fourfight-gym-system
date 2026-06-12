package com.gym.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.CheckoutRequest;
import com.gym.dto.request.PaymentFormRequest;
import com.gym.dto.response.CheckoutResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.CheckoutService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    
    private final CheckoutService checkoutService;
    
    @PostMapping
    public ResponseEntity<CheckoutResponse> initiateCheckout(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        CheckoutResponse response = checkoutService.processCheckout(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/{paymentId}/payment")
    public ResponseEntity<CheckoutResponse> processPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody PaymentFormRequest formRequest,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        CheckoutResponse response = checkoutService.processPaymentForm(paymentId, formRequest, principal);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<CheckoutResponse> getStatus(
            @PathVariable String paymentId,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        CheckoutResponse response = checkoutService.getCheckoutStatus(paymentId, principal);
        return ResponseEntity.ok(response);
    }
}

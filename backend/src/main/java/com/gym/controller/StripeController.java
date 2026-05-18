package com.gym.controller;

import com.gym.dto.response.StripeCheckoutResponse;
import com.gym.dto.response.StripeSubscriptionResponse;
import com.gym.entity.Membership;
import com.gym.entity.User;
import com.gym.repository.MembershipRepository;
import com.gym.service.StripeCheckoutService;
import com.gym.service.StripeWebhookService;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    private static final Logger log = LoggerFactory.getLogger(StripeController.class);

    private final StripeCheckoutService stripeCheckoutService;
    private final StripeWebhookService stripeWebhookService;
    private final MembershipRepository membershipRepository;

    public StripeController(
            StripeCheckoutService stripeCheckoutService,
            StripeWebhookService stripeWebhookService,
            MembershipRepository membershipRepository) {
        this.stripeCheckoutService = stripeCheckoutService;
        this.stripeWebhookService = stripeWebhookService;
        this.membershipRepository = membershipRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<StripeCheckoutResponse> createCheckoutSession(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {

        String planIdStr = body.get("planId");
        if (planIdStr == null || planIdStr.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        UUID planId;
        try {
            planId = UUID.fromString(planIdStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        try {
            StripeCheckoutResponse response = stripeCheckoutService.createCheckoutSession(user.getId(), planId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create Stripe checkout session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(HttpServletRequest request) {
        String payload;
        try (BufferedReader reader = request.getReader()) {
            payload = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("Failed to read webhook payload", e);
            return ResponseEntity.badRequest().build();
        }

        String sigHeader = request.getHeader("Stripe-Signature");
        if (sigHeader == null || sigHeader.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            stripeWebhookService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/subscription/cancel")
    public ResponseEntity<Void> cancelSubscription(@AuthenticationPrincipal User user) {
        List<Membership> memberships = membershipRepository.findByUserIdAndStatus(
                user.getId(), Membership.MembershipStatus.ACTIVE);
        Membership membership = memberships.isEmpty() ? null : memberships.get(0);

        if (membership == null || membership.getStripeSubscriptionId() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Subscription subscription = Subscription.retrieve(membership.getStripeSubscriptionId());
            subscription.cancel();
            membership.setCancelAtPeriodEnd(true);
            membershipRepository.save(membership);
            return ResponseEntity.ok().build();
        } catch (StripeException e) {
            log.error("Failed to cancel Stripe subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/subscription")
    public ResponseEntity<StripeSubscriptionResponse> getSubscription(@AuthenticationPrincipal User user) {
        List<Membership> memberships = membershipRepository.findByUserIdAndStatus(
                user.getId(), Membership.MembershipStatus.ACTIVE);
        Membership membership = memberships.isEmpty() ? null : memberships.get(0);

        if (membership == null) {
            return ResponseEntity.notFound().build();
        }

        StripeSubscriptionResponse response = new StripeSubscriptionResponse(
                membership.getId(),
                membership.getPlan().getName(),
                membership.getPlan().getPrice(),
                membership.getStatus().name(),
                membership.getCurrentPeriodStart(),
                membership.getCurrentPeriodEnd(),
                membership.getCancelAtPeriodEnd() != null && membership.getCancelAtPeriodEnd(),
                membership.getStripeSubscriptionId()
        );

        return ResponseEntity.ok(response);
    }
}

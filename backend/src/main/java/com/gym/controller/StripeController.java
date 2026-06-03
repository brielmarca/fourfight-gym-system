package com.gym.controller;

import com.gym.dto.response.StripeCheckoutResponse;
import com.gym.dto.response.StripeSubscriptionResponse;
import com.gym.dto.response.ReceptionRequestResponse;
import com.gym.dto.request.StripePlanSelectionRequest;
import com.gym.entity.Membership;
import com.gym.exception.BusinessRuleException;
import com.gym.repository.MembershipRepository;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.ReceptionCheckoutService;
import com.gym.service.StripeCheckoutService;
import com.gym.service.StripeWebhookService;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    private static final Logger log = LoggerFactory.getLogger(StripeController.class);

    private final StripeCheckoutService stripeCheckoutService;
    private final ReceptionCheckoutService receptionCheckoutService;
    private final StripeWebhookService stripeWebhookService;
    private final MembershipRepository membershipRepository;

    public StripeController(
            StripeCheckoutService stripeCheckoutService,
            ReceptionCheckoutService receptionCheckoutService,
            StripeWebhookService stripeWebhookService,
            MembershipRepository membershipRepository) {
        this.stripeCheckoutService = stripeCheckoutService;
        this.receptionCheckoutService = receptionCheckoutService;
        this.stripeWebhookService = stripeWebhookService;
        this.membershipRepository = membershipRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckoutSession(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody StripePlanSelectionRequest request) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            StripeCheckoutResponse response = stripeCheckoutService.createCheckoutSession(principal.id(), request.planId());
            return ResponseEntity.ok(response);
        } catch (BusinessRuleException e) {
            log.warn("Stripe checkout unavailable: {}", e.getMessage());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_GATEWAY,
                    "Servico de pagamento indisponivel no momento. Tente novamente em instantes."
            );
            problem.setTitle("Payment Provider Error");
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
        } catch (Exception e) {
            log.error("Failed to create Stripe checkout session", e);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Falha interna ao iniciar o checkout."
            );
            problem.setTitle("Checkout Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
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
        } catch (BusinessRuleException e) {
            log.warn("Invalid Stripe webhook signature");
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/subscription/cancel")
    public ResponseEntity<Void> cancelSubscription(@AuthenticationPrincipal JwtUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Membership> memberships = membershipRepository.findByUserIdAndStatus(
                principal.id(), Membership.MembershipStatus.ACTIVE);
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
    public ResponseEntity<StripeSubscriptionResponse> getSubscription(@AuthenticationPrincipal JwtUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Membership> memberships = membershipRepository.findByUserIdAndStatus(
                principal.id(), Membership.MembershipStatus.ACTIVE);
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

    @PostMapping("/reception-request")
    public ResponseEntity<?> createReceptionRequest(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody StripePlanSelectionRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ReceptionRequestResponse response = receptionCheckoutService.createReceptionRequest(principal.id(), request.planId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessRuleException e) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
            problem.setTitle("Reception Request Error");
            return ResponseEntity.badRequest().body(problem);
        }
    }

    @GetMapping("/reception-requests/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReceptionRequestResponse>> listPendingReceptionRequests() {
        return ResponseEntity.ok(receptionCheckoutService.listPendingRequests());
    }

    @PostMapping("/reception-requests/{membershipId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveReceptionRequest(@PathVariable UUID membershipId) {
        try {
            ReceptionRequestResponse response = receptionCheckoutService.approveRequest(membershipId);
            return ResponseEntity.ok(response);
        } catch (BusinessRuleException e) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
            problem.setTitle("Reception Approval Error");
            return ResponseEntity.badRequest().body(problem);
        }
    }

    @PostMapping("/reception-requests/{membershipId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectReceptionRequest(@PathVariable UUID membershipId) {
        try {
            ReceptionRequestResponse response = receptionCheckoutService.rejectRequest(membershipId);
            return ResponseEntity.ok(response);
        } catch (BusinessRuleException e) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
            problem.setTitle("Reception Rejection Error");
            return ResponseEntity.badRequest().body(problem);
        }
    }
}

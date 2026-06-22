package com.gym.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CancelMyMembershipRequest;
import com.gym.dto.request.CreateMembershipRequest;
import com.gym.dto.response.CancelMyMembershipResponse;
import com.gym.dto.response.MembershipResponse;
import com.gym.entity.Membership;
import com.gym.entity.Membership.MembershipStatus;
import com.gym.entity.Plan;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;

    public Page<MembershipResponse> getAll(Pageable pageable) {
        return membershipRepository.findAll(pageable).map(MembershipResponse::from);
    }

    public Page<MembershipResponse> getByUser(UUID userId, Pageable pageable) {
        return membershipRepository.findByUserId(userId, pageable).map(MembershipResponse::from);
    }

    @Transactional(readOnly = true)
    public MembershipResponse getByUserId(UUID userId) {
        List<Membership> memberships = membershipRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        if (memberships.isEmpty()) {
            throw new ResourceNotFoundException("Membership", userId);
        }
        return memberships.stream()
                .sorted(Comparator.comparingInt((Membership m) -> {
                    Membership.MembershipStatus s = m.getStatus();
                    if (s == Membership.MembershipStatus.ACTIVE) return 0;
                    if (s == Membership.MembershipStatus.EXPIRED) return 1;
                    if (s == Membership.MembershipStatus.CANCELLED) return 2;
                    return 3;
                }).thenComparing(Membership::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst()
                .map(MembershipResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", userId));
    }

    @Transactional(readOnly = true)
    public MembershipResponse getById(UUID id) {
        Membership membership = membershipRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Membership", id));
        return MembershipResponse.from(membership);
    }

    @Transactional
    public MembershipResponse create(CreateMembershipRequest request) {
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        Plan plan = planRepository.findById(request.planId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan", request.planId()));

        Membership membership = Membership.builder()
            .user(user)
            .plan(plan)
            .startDate(request.startDate())
            .endDate(request.startDate().plusDays(plan.getDurationDays()))
            .status(Membership.MembershipStatus.ACTIVE)
            .autoRenew(request.autoRenew() != null ? request.autoRenew() : false)
            .build();

        membership = membershipRepository.save(membership);
        log.info("Membership created for user: {} with plan: {}", user.getEmail(), plan.getName());
        return MembershipResponse.from(membership);
    }

    @Transactional
    public MembershipResponse renew(UUID id) {
        Membership membership = membershipRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Membership", id));

        LocalDate newEndDate = membership.getEndDate().plusDays(membership.getPlan().getDurationDays());
        membership.setEndDate(newEndDate);
        membership.setStatus(Membership.MembershipStatus.ACTIVE);
        membership = membershipRepository.save(membership);

        log.info("Membership renewed: {}", id);
        return MembershipResponse.from(membership);
    }

    @Transactional
    public MembershipResponse cancel(UUID id) {
        Membership membership = membershipRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Membership", id));
        membership.setStatus(Membership.MembershipStatus.CANCELLED);
        membership = membershipRepository.save(membership);

        log.info("Membership cancelled: {}", id);
        return MembershipResponse.from(membership);
    }

    @Transactional
    public CancelMyMembershipResponse cancelMyMembership(UUID userId, CancelMyMembershipRequest request) {
        List<Membership> activeMemberships = membershipRepository.findByUserIdAndStatus(userId, Membership.MembershipStatus.ACTIVE);
        if (activeMemberships.isEmpty()) {
            throw new BusinessRuleException("NO_ACTIVE_MEMBERSHIP", "Nao tens uma mensalidade ativa para cancelar.");
        }

        Membership membership = activeMemberships.get(0);

        if (Boolean.TRUE.equals(membership.getCancelAtPeriodEnd())) {
            LocalDate accessUntil = resolveAccessUntil(membership);
            return new CancelMyMembershipResponse(
                Membership.MembershipStatus.ACTIVE,
                true,
                accessUntil,
                "O cancelamento ja foi solicitado anteriormente. O teu acesso continua ativo ate " + accessUntil + "."
            );
        }

        String reason = request != null && request.reason() != null ? request.reason().trim() : null;
        if (reason != null && reason.length() > 500) {
            reason = reason.substring(0, 500);
        }

        String stripeSubscriptionId = membership.getStripeSubscriptionId();
        if (stripeSubscriptionId != null && !stripeSubscriptionId.isBlank()) {
            try {
                Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
                Map<String, Object> updateParams = new HashMap<>();
                updateParams.put("cancel_at_period_end", true);
                subscription = subscription.update(updateParams);

                if (subscription.getCurrentPeriodEnd() != null) {
                    membership.setCurrentPeriodEnd(
                        LocalDate.ofInstant(
                            java.time.Instant.ofEpochSecond(subscription.getCurrentPeriodEnd()),
                            java.time.ZoneId.systemDefault()
                        )
                    );
                    membership.setEndDate(membership.getCurrentPeriodEnd());
                }
            } catch (StripeException e) {
                log.error("Stripe API error scheduling cancellation for membership {}: {}", membership.getId(), e.getMessage());
                throw new BusinessRuleException("STRIPE_ERROR", "Nao foi possivel comunicar com o servico de pagamento. Tenta novamente mais tarde.");
            }
        }

        membership.setCancelAtPeriodEnd(true);
        membership.setAutoRenew(false);
        membership.setCancellationRequestedAt(LocalDateTime.now());
        membership.setCancellationReason(reason);
        membership.setCancellationSource(Membership.CancellationSource.STUDENT_SELF_SERVICE.name());
        membershipRepository.save(membership);

        LocalDate accessUntil = resolveAccessUntil(membership);

        log.info("Membership cancellation scheduled for user {}: {}", userId, membership.getId());

        return new CancelMyMembershipResponse(
            Membership.MembershipStatus.ACTIVE,
            true,
            accessUntil,
            "Cancelamento agendado. O teu acesso continua ativo ate " + accessUntil + "."
        );
    }

    private LocalDate resolveAccessUntil(Membership membership) {
        if (membership.getCurrentPeriodEnd() != null) {
            return membership.getCurrentPeriodEnd();
        }
        return membership.getEndDate();
    }

    public long countActive() {
        return membershipRepository.countActive();
    }
}

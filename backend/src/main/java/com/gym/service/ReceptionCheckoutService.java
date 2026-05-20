package com.gym.service;

import com.gym.dto.response.ReceptionRequestResponse;
import com.gym.entity.Membership;
import com.gym.entity.Payment;
import com.gym.entity.Plan;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceptionCheckoutService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public ReceptionRequestResponse createReceptionRequest(UUID userId, UUID planId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));

        if (!Boolean.TRUE.equals(plan.getIsActive())) {
            throw new BusinessRuleException("Selected plan is not active");
        }

        boolean alreadyPending = membershipRepository.findByUserIdAndStatus(userId, Membership.MembershipStatus.PENDING_APPROVAL)
            .stream()
            .anyMatch(m -> m.getPlan().getId().equals(planId));
        if (alreadyPending) {
            throw new BusinessRuleException("You already have a pending reception request for this plan");
        }

        boolean alreadyActive = membershipRepository.findByUserIdAndStatus(userId, Membership.MembershipStatus.ACTIVE)
            .stream()
            .anyMatch(m -> m.getPlan().getId().equals(planId));
        if (alreadyActive) {
            throw new BusinessRuleException("You already have an active membership for this plan");
        }

        LocalDate provisionalStart = LocalDate.now();
        Membership membership = Membership.builder()
            .user(user)
            .plan(plan)
            .startDate(provisionalStart)
            .endDate(provisionalStart.plusDays(plan.getDurationDays()))
            .status(Membership.MembershipStatus.PENDING_APPROVAL)
            .autoRenew(false)
            .build();
        membership = membershipRepository.save(membership);

        Payment payment = Payment.builder()
            .user(user)
            .membership(membership)
            .amount(plan.getPrice())
            .currency(plan.getCurrency())
            .method(Payment.PaymentMethod.RECEPTION)
            .status(Payment.PaymentStatus.PENDING)
            .build();
        payment = paymentRepository.save(payment);

        return toResponse(membership, payment, "Pedido enviado. A sua adesao ficara pendente ate aprovacao na rececao.");
    }

    @Transactional(readOnly = true)
    public List<ReceptionRequestResponse> listPendingRequests() {
        return membershipRepository.findByStatusOrderByCreatedAtDesc(Membership.MembershipStatus.PENDING_APPROVAL)
            .stream()
            .map(membership -> {
                Payment payment = paymentRepository.findFirstByMembershipIdOrderByCreatedAtDesc(membership.getId())
                    .orElse(null);
                return toResponse(membership, payment, "Pendente de aprovacao");
            })
            .toList();
    }

    @Transactional
    public ReceptionRequestResponse approveRequest(UUID membershipId) {
        Membership membership = membershipRepository.findById(membershipId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership", membershipId));

        if (membership.getStatus() != Membership.MembershipStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("Only pending reception requests can be approved");
        }

        Payment payment = paymentRepository.findFirstByMembershipIdOrderByCreatedAtDesc(membershipId)
            .orElseThrow(() -> new BusinessRuleException("Reception payment request not found"));

        if (payment.getMethod() != Payment.PaymentMethod.RECEPTION) {
            throw new BusinessRuleException("Payment request is not a reception request");
        }

        LocalDate startDate = LocalDate.now();
        membership.setStartDate(startDate);
        membership.setEndDate(startDate.plusDays(membership.getPlan().getDurationDays()));
        membership.setStatus(Membership.MembershipStatus.ACTIVE);
        membershipRepository.save(membership);

        payment.complete("RECEPTION_APPROVED", "{\"status\":\"approved\"}");
        paymentRepository.save(payment);

        return toResponse(membership, payment, "Pedido aprovado. A adesao foi ativada.");
    }

    @Transactional
    public ReceptionRequestResponse rejectRequest(UUID membershipId) {
        Membership membership = membershipRepository.findById(membershipId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership", membershipId));

        if (membership.getStatus() != Membership.MembershipStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("Only pending reception requests can be rejected");
        }

        Payment payment = paymentRepository.findFirstByMembershipIdOrderByCreatedAtDesc(membershipId)
            .orElseThrow(() -> new BusinessRuleException("Reception payment request not found"));

        if (payment.getMethod() != Payment.PaymentMethod.RECEPTION) {
            throw new BusinessRuleException("Payment request is not a reception request");
        }

        membership.setStatus(Membership.MembershipStatus.REJECTED);
        membershipRepository.save(membership);

        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        return toResponse(membership, payment, "Pedido rejeitado. A adesao nao foi ativada.");
    }

    private ReceptionRequestResponse toResponse(Membership membership, Payment payment, String message) {
        return new ReceptionRequestResponse(
            membership.getId(),
            membership.getUser().getId(),
            membership.getUser().getName(),
            membership.getUser().getEmail(),
            membership.getPlan().getId(),
            membership.getPlan().getName(),
            membership.getPlan().getPrice(),
            membership.getStatus(),
            payment != null ? payment.getMethod().name() : "RECEPTION",
            payment != null ? payment.getStatus().name() : "PENDING",
            message,
            membership.getCreatedAt()
        );
    }
}

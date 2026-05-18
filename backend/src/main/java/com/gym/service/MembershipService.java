package com.gym.service;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateMembershipRequest;
import com.gym.dto.response.MembershipResponse;
import com.gym.entity.Membership;
import com.gym.entity.Membership.MembershipStatus;
import com.gym.entity.Plan;
import com.gym.entity.User;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.UserRepository;
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

    public MembershipResponse getByUserId(UUID userId) {
        return membershipRepository.findByUserIdAndStatus(userId, Membership.MembershipStatus.ACTIVE)
                .stream()
                .findFirst()
                .map(MembershipResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Active Membership", userId));
    }

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

    public long countActive() {
        return membershipRepository.countActive();
    }
}
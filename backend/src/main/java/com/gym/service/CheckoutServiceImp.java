package com.gym.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CheckoutRequest;
import com.gym.dto.request.PaymentFormRequest;
import com.gym.dto.response.CheckoutResponse;
import com.gym.entity.Membership;
import com.gym.entity.Payment;
import com.gym.entity.Plan;
import com.gym.entity.Student;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.RefreshTokenRepository;
import com.gym.repository.StudentRepository;
import com.gym.repository.UserRepository;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutServiceImp implements CheckoutService {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CheckoutResponse processCheckout(CheckoutRequest request, JwtUserPrincipal principal) {
        log.info("Processing checkout for authenticated user: {}", principal.id());

        // 1. Get authenticated user from JWT (do NOT trust request data for user identity)
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.id()));

        // 2. Get the plan
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + request.planId()));
        if (!Boolean.TRUE.equals(plan.getIsActive())) {
            throw new BusinessRuleException("Plan is not available");
        }

        // 3. Create or update student record
        Student student = studentRepository.findByEmail(user.getEmail()).orElse(null);
        if (student == null) {
            student = Student.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .plan(plan)
                    .isActive(true)
                    .build();
            student = studentRepository.save(student);
            log.info("Created student record: {}", student.getId());
        } else {
            student.setPlan(plan);
            student.setIsActive(true);
            studentRepository.save(student);
        }

        Payment.PaymentMethod paymentMethod;
        try {
            paymentMethod = Payment.PaymentMethod.valueOf(request.paymentMethod().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Unsupported payment method");
        }

        Membership.MembershipStatus pendingStatus = paymentMethod == Payment.PaymentMethod.RECEPTION
                ? Membership.MembershipStatus.PENDING_APPROVAL
                : Membership.MembershipStatus.PENDING_PAYMENT;

        // 4. Create membership linked to authenticated user
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());

        Membership membership = Membership.builder()
                .user(user)
                .plan(plan)
                .startDate(startDate)
                .endDate(endDate)
                .status(pendingStatus)
                .autoRenew(false)
                .build();
        membership = membershipRepository.save(membership);
        log.info("Created membership: {} for user: {} with plan: {}", membership.getId(), user.getEmail(), plan.getName());

        // 5. Create payment record (initially PENDING)
        Payment payment = Payment.builder()
                .user(user)
                .membership(membership)
                .amount(plan.getPrice())
                .currency(plan.getCurrency())
                .method(paymentMethod)
                .status(Payment.PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);
        log.info("Created payment record: {} with status: PENDING", payment.getId());

        return CheckoutResponse.builder()
                .id(payment.getId())
                .name(user.getName())
                .email(user.getEmail())
                .planName(plan.getName())
                .planPrice(plan.getPrice())
                .paymentMethod(request.paymentMethod())
                .paymentStatus(Payment.PaymentStatus.PENDING)
                .message(paymentMethod == Payment.PaymentMethod.RECEPTION
                        ? "Pedido enviado. Aguardando aprovacao na rececao."
                        : "Checkout initiated. Proceed to Stripe payment.")
                .userId(user.getId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public CheckoutResponse processPaymentForm(String paymentId, PaymentFormRequest formRequest, JwtUserPrincipal principal) {
        Payment payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        // Verify payment belongs to authenticated user
        if (!payment.getUser().getId().equals(principal.id())) {
            throw new SecurityException("Payment does not belong to authenticated user");
        }

        // Demo/manual processing methods were removed from production flow.
        Payment.PaymentMethod method = payment.getMethod();
        if (method == Payment.PaymentMethod.STRIPE) {
            throw new BusinessRuleException("Stripe payments must be completed through /api/stripe/checkout and webhooks");
        }

        if (method == Payment.PaymentMethod.RECEPTION) {
            return CheckoutResponse.builder()
                    .id(payment.getId())
                    .name(payment.getUser().getName())
                    .email(payment.getUser().getEmail())
                    .planName(payment.getMembership().getPlan().getName())
                    .planPrice(payment.getMembership().getPlan().getPrice())
                    .paymentMethod(method.name())
                    .paymentStatus(Payment.PaymentStatus.PENDING)
                    .message("Pedido enviado. Aguardando aprovacao na rececao.")
                    .userId(payment.getUser().getId())
                    .createdAt(payment.getCreatedAt())
                    .build();
        }

        throw new BusinessRuleException("Demo payment methods are disabled");
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutResponse getCheckoutStatus(String paymentId, JwtUserPrincipal principal) {
        Payment payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        boolean isOwner = payment.getUser().getId().equals(principal.id());
        boolean isAdmin = "ADMIN".equals(principal.role());
        if (!isOwner && !isAdmin) {
            throw new ResourceNotFoundException("Payment", paymentId);
        }

        return CheckoutResponse.builder()
                .id(payment.getId())
                .name(payment.getUser().getName())
                .email(payment.getUser().getEmail())
                .planName(payment.getMembership().getPlan().getName())
                .planPrice(payment.getMembership().getPlan().getPrice())
                .paymentMethod(payment.getMethod().name())
                .paymentStatus(payment.getStatus())
                .message("Status: " + payment.getStatus())
                .userId(payment.getUser().getId())
                .createdAt(payment.getPaidAt())
                .build();
    }
}

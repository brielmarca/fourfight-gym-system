package com.gym.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreatePaymentRequest;
import com.gym.dto.response.PaymentResponse;
import com.gym.entity.Membership;
import com.gym.entity.Payment;
import com.gym.entity.Payment.PaymentMethod;
import com.gym.entity.Payment.PaymentStatus;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public Page<PaymentResponse> getAll(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(PaymentResponse::from);
    }

    public Page<PaymentResponse> getByUser(UUID userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable).map(PaymentResponse::from);
    }

    public PaymentResponse getById(UUID id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse create(CreatePaymentRequest request) {
        Membership membership = membershipRepository.findById(request.membershipId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership", request.membershipId()));

        Payment payment = Payment.builder()
                .user(membership.getUser())
                .membership(membership)
                .amount(request.amount())
                .currency(request.currency() != null ? request.currency() : "BRL")
                .method(request.method() != null ? request.method() : Payment.PaymentMethod.CARD)
                .status(Payment.PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment created: {} {}", payment.getAmount(), payment.getCurrency());
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse complete(UUID id, String gatewayRef, String gatewayResponse) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", id));

        payment.complete(gatewayRef, gatewayResponse);
        payment = paymentRepository.save(payment);
        log.info("Payment completed: {}", id);
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse refund(UUID id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", id));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new BusinessRuleException("REFUND_NOT_ALLOWED", "Only completed payments can be refunded");
        }

        payment.refund();
        payment = paymentRepository.save(payment);
        log.info("Payment refunded: {}", id);
        return PaymentResponse.from(payment);
    }

    public java.math.BigDecimal getRevenueMTD() {
        java.time.LocalDateTime startOfMonth = java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return paymentRepository.sumCompletedSince(startOfMonth);
    }
}
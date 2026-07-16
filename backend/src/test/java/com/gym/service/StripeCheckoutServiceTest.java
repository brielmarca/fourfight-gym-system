package com.gym.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gym.entity.Plan;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StripeCheckoutServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @InjectMocks
    private StripeCheckoutService service;

    @Test
    void createCheckoutSessionRejectsPlanWithoutConfiguredStripePrice() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        Plan plan = Plan.builder()
                .id(planId)
                .price(new BigDecimal("38.50"))
                .currency("EUR")
                .durationDays(30)
                .isActive(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(planRepository.findById(planId)).thenReturn(java.util.Optional.of(plan));

        assertThatThrownBy(() -> service.createCheckoutSession(userId, planId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Stripe indisponível");

        verifyNoInteractions(membershipRepository);
    }
}

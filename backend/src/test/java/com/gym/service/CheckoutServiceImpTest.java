package com.gym.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gym.dto.request.CheckoutRequest;
import com.gym.entity.Plan;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.RefreshTokenRepository;
import com.gym.repository.StudentRepository;
import com.gym.repository.UserRepository;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImpTest {

    @Mock
    private PlanRepository planRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CheckoutServiceImp service;

    @Test
    void processCheckoutRejectsInactivePlanBeforeCreatingSaleRecords() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User user = User.builder().id(userId).email("student@example.com").build();
        Plan plan = Plan.builder().id(planId).isActive(false).build();
        CheckoutRequest request = new CheckoutRequest(
                "Student", "student@example.com", "Password1", planId, "RECEPTION");
        JwtUserPrincipal principal = new JwtUserPrincipal(
                userId, user.getEmail(), "encoded", "STUDENT", true, List.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> service.processCheckout(request, principal))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Plan is not available");

        verifyNoInteractions(studentRepository, membershipRepository, paymentRepository);
    }
}

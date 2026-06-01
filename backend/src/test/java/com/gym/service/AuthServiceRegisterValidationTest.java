package com.gym.service;

import com.gym.dto.request.PreferredContactMethod;
import com.gym.dto.request.PreferredModality;
import com.gym.dto.request.PreferredTrainingDay;
import com.gym.dto.request.PreferredTrainingTime;
import com.gym.dto.request.RegisterRequest;
import com.gym.entity.PreRegistrationProfile;
import com.gym.entity.User;
import com.gym.exception.ValidationException;
import com.gym.repository.PreRegistrationProfileRepository;
import com.gym.repository.RefreshTokenRepository;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterValidationTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PreRegistrationProfileRepository preRegistrationProfileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private ApplicationEventPublisher eventPublisher;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            userRepository,
            refreshTokenRepository,
            preRegistrationProfileRepository,
            passwordEncoder,
            jwtUtil,
            eventPublisher
        );

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        lenient().when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(UUID.randomUUID());
            }
            return user;
        });
        lenient().when(preRegistrationProfileRepository.save(any(PreRegistrationProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rejectsFutureDateOfBirth() {
        RegisterRequest request = validRequest(LocalDate.now().plusDays(1), 20);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Validation failed");
    }

    @Test
    void rejectsTooYoungAgeCalculatedFromDateOfBirth() {
        RegisterRequest request = validRequest(LocalDate.now().minusYears(2), 2);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Validation failed");
    }

    @Test
    void rejectsManipulatedClientAgeWhenDoesNotMatchCalculatedAge() {
        LocalDate dob = LocalDate.now().minusYears(20).minusDays(1);
        int calculatedAge = Period.between(dob, LocalDate.now()).getYears();
        RegisterRequest request = validRequest(dob, calculatedAge + 3);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Validation failed");
    }

    @Test
    void acceptsValidDateOfBirthAndStoresServerCalculatedAge() {
        LocalDate dob = LocalDate.now().minusYears(27).minusMonths(3);
        int calculatedAge = Period.between(dob, LocalDate.now()).getYears();
        RegisterRequest request = validRequest(dob, calculatedAge);

        authService.register(request);

        ArgumentCaptor<PreRegistrationProfile> profileCaptor = ArgumentCaptor.forClass(PreRegistrationProfile.class);
        verify(preRegistrationProfileRepository).save(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getAge()).isEqualTo(calculatedAge);
    }

    private RegisterRequest validRequest(LocalDate dateOfBirth, int age) {
        return new RegisterRequest(
            "Test User",
            "test@example.com",
            "Password1",
            "+351912345678",
            dateOfBirth,
            age,
            "Lisboa",
            true,
            "Jiu-jitsu por 1 ano",
            "Melhorar condicao fisica",
            PreferredModality.JIU_JITSU,
            null,
            PreferredTrainingTime.NIGHT_AFTER_18,
            null,
            List.of(PreferredTrainingDay.MONDAY),
            true,
            PreferredContactMethod.MESSAGE,
            null
        );
    }
}

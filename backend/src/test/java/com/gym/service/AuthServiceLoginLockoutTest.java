package com.gym.service;

import com.gym.entity.User;
import com.gym.repository.UserRepository;
import com.gym.security.RateLimitFilter;
import com.gym.security.RateLimitFilterTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "rate-limit.login.capacity=1000",
    "rate-limit.login.refill-tokens=1000",
    "rate-limit.login.refill-duration=1",
    "security.lockout.failed-attempts=5",
    "security.lockout.duration=PT15M",
    "security.lockout.cache-expiry=PT30M",
    "mfa.encryption-key-base64=VEVTVC1LRVktTk9ULUZPUi1QUk9EVUNUSU9OLTAxMjM="
})
@AutoConfigureMockMvc
class AuthServiceLoginLockoutTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private RateLimitFilter rateLimitFilter;
    @Autowired private AuthService authService;

    private static final String PASSWORD = "Password123!";

    @BeforeEach
    void setUp() {
        RateLimitFilterTestSupport.reset(rateLimitFilter);
        authService.setClock(Clock.systemUTC());
    }

    private User createTestUser(String email) {
        User user = User.builder()
            .id(UUID.randomUUID())
            .email(email)
            .name("Test User")
            .passwordHash(passwordEncoder.encode(PASSWORD))
            .role(User.Role.CLIENT)
            .isActive(true)
            .phone("+5511999999999")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        return userRepository.save(user);
    }

    private void failLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"wrongpass\"}".formatted(email)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void accountAFailuresDoNotLockAccountB() throws Exception {
        String emailA = createTestUser("lockout-a-" + System.nanoTime() + "@test.com").getEmail();
        String emailB = createTestUser("lockout-b-" + System.nanoTime() + "@test.com").getEmail();

        for (int i = 0; i < 5; i++) {
            failLogin(emailA);
        }

        assertLoginLocked(emailA);
        assertLoginOk(emailB);
    }

    @Test
    void successClearsOnlyCurrentAccount() throws Exception {
        String emailA = createTestUser("success-a-" + System.nanoTime() + "@test.com").getEmail();
        String emailB = createTestUser("success-b-" + System.nanoTime() + "@test.com").getEmail();

        for (int i = 0; i < 3; i++) {
            failLogin(emailA);
        }
        for (int i = 0; i < 3; i++) {
            failLogin(emailB);
        }

        assertLoginOk(emailA);

        failLogin(emailB);
        failLogin(emailB);
        assertLoginLocked(emailB);
    }

    @Test
    void unknownUserAttemptsDoNotAffectKnownUsers() throws Exception {
        String emailA = createTestUser("lockout-unknown-" + System.nanoTime() + "@test.com").getEmail();

        for (int i = 0; i < 10; i++) {
            failLogin("nonexistent" + i + "@test.com");
        }

        assertLoginOk(emailA);
    }

    @Test
    void caseVariantEmailResolvesSameLockout() throws Exception {
        String emailLower = createTestUser("lockout-case-" + System.nanoTime() + "@test.com").getEmail();

        for (int i = 0; i < 5; i++) {
            failLogin(emailLower.toUpperCase());
        }

        assertLoginLocked(emailLower);
    }

    @Test
    void leadingTrailingWhitespaceSharesState() throws Exception {
        String email = createTestUser("lockout-ws-" + System.nanoTime() + "@test.com").getEmail();

        for (int i = 0; i < 5; i++) {
            failLogin("  " + email + "  ");
        }

        assertLoginLocked(email);
    }

    @Test
    void thresholdIsExact() throws Exception {
        String email = createTestUser("lockout-threshold-" + System.nanoTime() + "@test.com").getEmail();

        for (int i = 0; i < 4; i++) {
            failLogin(email);
        }
        assertLoginOk(email);

        for (int i = 0; i < 5; i++) {
            failLogin(email);
        }
        assertLoginLocked(email);
    }

    @Test
    void lockActiveForConfiguredDuration() throws Exception {
        String email = createTestUser("lockout-duration-" + System.nanoTime() + "@test.com").getEmail();
        Clock fixedClock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);
        authService.setClock(fixedClock);

        for (int i = 0; i < 5; i++) {
            failLogin(email);
        }

        authService.setClock(Clock.fixed(Instant.parse("2026-06-01T12:10:00Z"), ZoneOffset.UTC));
        assertLoginLocked(email);
    }

    @Test
    void lockExpiresDeterministically() throws Exception {
        String email = createTestUser("lockout-expire-" + System.nanoTime() + "@test.com").getEmail();
        Clock fixedClock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);
        authService.setClock(fixedClock);

        for (int i = 0; i < 5; i++) {
            failLogin(email);
        }

        authService.setClock(Clock.fixed(Instant.parse("2026-06-01T12:16:00Z"), ZoneOffset.UTC));
        assertLoginOk(email);
    }

    @Test
    void successfulLoginAfterLockExpiryWorks() throws Exception {
        String email = createTestUser("lockout-after-" + System.nanoTime() + "@test.com").getEmail();
        Clock fixedClock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);
        authService.setClock(fixedClock);

        for (int i = 0; i < 5; i++) {
            failLogin(email);
        }

        authService.setClock(Clock.fixed(Instant.parse("2026-06-01T12:16:00Z"), ZoneOffset.UTC));
        assertLoginOk(email);
    }

    @Test
    void lockoutResponseDoesNotEnumerate() throws Exception {
        String emailA = createTestUser("lockout-enum-" + System.nanoTime() + "@test.com").getEmail();

        failLogin(emailA);

        String unknownResponse = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"unknown-%s@test.com\",\"password\":\"wrongpass\"}".formatted(System.nanoTime())))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();

        String lockedResponse = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"wrongpass\"}".formatted(emailA)))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();

        String wrongPassResponse = getWrongPasswordResponse("lockout-gen-" + System.nanoTime() + "@test.com");

        assertThat(unknownResponse).doesNotContain("email not found", "not registered", "does not exist");
        assertThat(wrongPassResponse).doesNotContain("email not found", "not registered", "does not exist");
        assertThat(lockedResponse).doesNotContain("email not found", "not registered", "does not exist");
    }

    @Test
    void genericResponsesForWrongPasswordUnknownAndLocked() throws Exception {
        String email = createTestUser("lockout-generic-" + System.nanoTime() + "@test.com").getEmail();

        String wrongPassResponse = getWrongPasswordResponse("lockout-gen2-" + System.nanoTime() + "@test.com");

        String unknownResponse = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"unknown-%s@test.com\",\"password\":\"wrongpass\"}".formatted(System.nanoTime())))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();

        for (int i = 0; i < 5; i++) {
            failLogin(email);
        }

        String lockedResponse = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();

        assertThat(unknownResponse).contains("Invalid email or password");
        assertThat(wrongPassResponse).contains("Invalid email or password");
        assertThat(lockedResponse).contains("Invalid email or password");
    }

    @Test
    void concurrentFailuresForOneAccountAreNotLost() throws Exception {
        String email = createTestUser("lockout-concur-" + System.nanoTime() + "@test.com").getEmail();
        int threadCount = 10;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    barrier.await();
                    mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"wrongpass\"}".formatted(email)));
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            });
        }
        executor.shutdown();
        assertThat(executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        assertThat(errors).hasValue(0);

        assertLoginLocked(email);
    }

    @Test
    void differentAccountsUpdateIndependentlyUnderConcurrency() throws Exception {
        String emailA = createTestUser("lockout-conA-" + System.nanoTime() + "@test.com").getEmail();
        String emailB = createTestUser("lockout-conB-" + System.nanoTime() + "@test.com").getEmail();
        int threadCount = 10;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final String email = i % 2 == 0 ? emailA : emailB;
            executor.submit(() -> {
                try {
                    barrier.await();
                    mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"wrongpass\"}".formatted(email)));
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            });
        }
        executor.shutdown();
        assertThat(executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        assertThat(errors).hasValue(0);

        assertLoginLocked(emailA);
        assertLoginLocked(emailB);
    }

    @Test
    void mfaPreAuthNotIssuedWhileLocked() throws Exception {
        String email = createTestUser("lockout-mfa-" + System.nanoTime() + "@test.com").getEmail();
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        user.setMfaEnabled(true);
        userRepository.save(user);

        for (int i = 0; i < 5; i++) {
            failLogin(email);
        }

        assertLoginLocked(email);
    }

    @Test
    void unknownEmailFloodingDoesNotAffectKnownUsers() throws Exception {
        for (int i = 0; i < 100; i++) {
            failLogin("flood-" + i + "-" + System.nanoTime() + "@test.com");
        }

        String email = createTestUser("lockout-flood-" + System.nanoTime() + "@test.com").getEmail();
        assertLoginOk(email);
    }

    @Test
    void disabledAccountReturnsGenericResponse() throws Exception {
        String email = createTestUser("lockout-disabled-" + System.nanoTime() + "@test.com").getEmail();
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        user.setIsActive(false);
        userRepository.save(user);

        String disabledResponse = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();

        assertThat(disabledResponse).contains("Invalid email or password");
    }

    @Test
    void allAuthFailuresReturnIdenticalDetail() throws Exception {
        String email = createTestUser("lockout-idents-" + System.nanoTime() + "@test.com").getEmail();

        String unknownDetail = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"unknown-%s@test.com\",\"password\":\"wrongpass\"}".formatted(System.nanoTime())))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();

        String wrongPassDetail = getWrongPasswordResponse("lockout-idents2-" + System.nanoTime() + "@test.com");

        for (int i = 0; i < 5; i++) {
            failLogin(email);
        }

        String lockedDetail = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();

        assertThat(unknownDetail).contains("Invalid email or password");
        assertThat(wrongPassDetail).contains("Invalid email or password");
        assertThat(lockedDetail).contains("Invalid email or password");
    }

    private String getWrongPasswordResponse(String email) throws Exception {
        createTestUser(email);
        return mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"wrongpass\"}".formatted(email)))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();
    }

    private void assertLoginLocked(String email) throws Exception {
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value(containsString("Invalid email or password")));
    }

    private void assertLoginOk(String email) throws Exception {
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
            .andExpect(status().isOk());
    }
}

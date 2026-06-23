package com.gym.controller.security;

import com.gym.entity.Membership;
import com.gym.entity.Plan;
import com.gym.entity.User;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
import com.gym.security.RateLimitFilter;
import com.gym.security.RateLimitFilterTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "rate-limit.login.capacity=100",
        "rate-limit.login.refill-tokens=100",
        "rate-limit.login.refill-duration=1"
})
@AutoConfigureMockMvc
class MembershipCancelSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    private String clientToken;
    private String adminToken;
    private String managerToken;
    private String trainerToken;
    private UUID clientMembershipId;
    private UUID otherMembershipId;

    @BeforeEach
    void setUp() {
        RateLimitFilterTestSupport.reset(rateLimitFilter);
        User client = userRepository.findByEmail("cancel-test-client@test.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .id(UUID.randomUUID())
                        .name("Cancel Test Client")
                        .email("cancel-test-client@test.com")
                        .passwordHash("dummy")
                        .role(User.Role.CLIENT)
                        .isActive(true)
                        .build()));

        User admin = userRepository.findByEmail("cancel-test-admin@test.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .id(UUID.randomUUID())
                        .name("Cancel Test Admin")
                        .email("cancel-test-admin@test.com")
                        .passwordHash("dummy")
                        .role(User.Role.ADMIN)
                        .isActive(true)
                        .build()));

        User manager = userRepository.findByEmail("cancel-test-manager@test.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .id(UUID.randomUUID())
                        .name("Cancel Test Manager")
                        .email("cancel-test-manager@test.com")
                        .passwordHash("dummy")
                        .role(User.Role.MANAGER)
                        .isActive(true)
                        .build()));

        User trainer = userRepository.findByEmail("cancel-test-trainer@test.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .id(UUID.randomUUID())
                        .name("Cancel Test Trainer")
                        .email("cancel-test-trainer@test.com")
                        .passwordHash("dummy")
                        .role(User.Role.TRAINER)
                        .isActive(true)
                        .build()));

        User otherUser = userRepository.findByEmail("cancel-test-other@test.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .id(UUID.randomUUID())
                        .name("Cancel Test Other")
                        .email("cancel-test-other@test.com")
                        .passwordHash("dummy")
                        .role(User.Role.CLIENT)
                        .isActive(true)
                        .build()));

        Plan plan = planRepository.findAll().stream().findFirst().orElseGet(() ->
                planRepository.save(Plan.builder()
                        .name("Cancel Test Plan")
                        .description("Plan for cancel tests")
                        .price(new BigDecimal("49.90"))
                        .currency("BRL")
                        .durationDays(30)
                        .isActive(true)
                        .build()));

        Membership clientMembership = membershipRepository.save(Membership.builder()
                .user(client)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(Membership.MembershipStatus.ACTIVE)
                .autoRenew(false)
                .build());
        clientMembershipId = clientMembership.getId();

        Membership otherMembership = membershipRepository.save(Membership.builder()
                .user(otherUser)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(Membership.MembershipStatus.ACTIVE)
                .autoRenew(false)
                .build());
        otherMembershipId = otherMembership.getId();

        clientToken = jwtUtil.generateAccessToken(client.getId(), client.getEmail(), "CLIENT");
        adminToken = jwtUtil.generateAccessToken(admin.getId(), admin.getEmail(), "ADMIN");
        managerToken = jwtUtil.generateAccessToken(manager.getId(), manager.getEmail(), "MANAGER");
        trainerToken = jwtUtil.generateAccessToken(trainer.getId(), trainer.getEmail(), "TRAINER");
    }

    @Test
    @DisplayName("Unauthenticated cancel membership returns 401")
    void unauthenticatedCancelReturns401() throws Exception {
        mockMvc.perform(patch("/api/memberships/" + clientMembershipId + "/cancel"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CLIENT cancel membership returns 403")
    void clientCancelReturns403() throws Exception {
        mockMvc.perform(patch("/api/memberships/" + clientMembershipId + "/cancel")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TRAINER cancel membership returns 403")
    void trainerCancelReturns403() throws Exception {
        mockMvc.perform(patch("/api/memberships/" + otherMembershipId + "/cancel")
                        .header("Authorization", "Bearer " + trainerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN cancel membership returns 200 and CANCELLED status")
    void adminCancelReturns200() throws Exception {
        mockMvc.perform(patch("/api/memberships/" + otherMembershipId + "/cancel")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Membership db = membershipRepository.findById(otherMembershipId).orElseThrow();
        assert db.getStatus() == Membership.MembershipStatus.CANCELLED : "DB status must be CANCELLED";
    }

    @Test
    @DisplayName("MANAGER cancel membership returns 200 and CANCELLED status")
    void managerCancelReturns200() throws Exception {
        Plan plan = planRepository.findAll().stream().findFirst().orElseThrow();
        User otherUser = userRepository.findByEmail("cancel-test-other@test.com").orElseThrow();
        Membership fresh = membershipRepository.save(Membership.builder()
                .user(otherUser)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(Membership.MembershipStatus.ACTIVE)
                .autoRenew(false)
                .build());

        mockMvc.perform(patch("/api/memberships/" + fresh.getId() + "/cancel")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Membership db = membershipRepository.findById(fresh.getId()).orElseThrow();
        assert db.getStatus() == Membership.MembershipStatus.CANCELLED : "DB status must be CANCELLED";
    }

    @Test
    @DisplayName("ADMIN cancel with nonexistent ID returns 404")
    void adminCancelNonexistentReturns404() throws Exception {
        mockMvc.perform(patch("/api/memberships/" + UUID.randomUUID() + "/cancel")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Rate limit returns 429 after bucket exhaustion and recovers after reset")
    void rateLimitReturns429AfterExhaustion() throws Exception {
        int callCount;
        for (callCount = 0; callCount < 200; callCount++) {
            int status = mockMvc.perform(get("/api/rl-exhaust-test-" + callCount))
                    .andReturn().getResponse().getStatus();
            if (status == 429) {
                break;
            }
        }
        assertTrue(callCount < 200, "Rate limit should have been triggered within 200 calls");
        assertTrue(callCount >= 100, "Should take at least 100 calls to exhaust general bucket");

        RateLimitFilterTestSupport.reset(rateLimitFilter);

        int statusAfterReset = mockMvc.perform(get("/api/rl-after-reset"))
                .andReturn().getResponse().getStatus();
        assertNotEquals(429, statusAfterReset, "Should not be 429 after bucket reset");
    }

    @Test
    @DisplayName("POST on /{id}/cancel does not succeed")
    void postOnCancelEndpointDoesNotSucceed() throws Exception {
        Plan plan = planRepository.findAll().stream().findFirst().orElseThrow();
        User otherUser = userRepository.findByEmail("cancel-test-other@test.com").orElseThrow();
        Membership fresh = membershipRepository.save(Membership.builder()
                .user(otherUser)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(Membership.MembershipStatus.ACTIVE)
                .autoRenew(false)
                .build());

        mockMvc.perform(post("/api/memberships/" + fresh.getId() + "/cancel")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 200) {
                        throw new AssertionError("POST should not succeed on PATCH-only endpoint, got 200");
                    }
                });
    }
}

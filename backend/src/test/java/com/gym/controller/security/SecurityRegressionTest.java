package com.gym.controller.security;

import com.gym.entity.User;
import com.gym.entity.Plan;
import com.gym.entity.Membership;
import com.gym.entity.Payment;
import com.gym.entity.ScheduleRequest;
import com.gym.entity.Trainer;
import com.gym.repository.PaymentRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.MembershipRepository;
import com.gym.repository.ScheduleRequestRepository;
import com.gym.repository.TrainerRepository;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
import com.gym.service.AuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockCookie;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security regression tests for recent hardening changes.
 * Focuses on JWT validation and endpoint protection.
 */
@SpringBootTest(properties = {
        "rate-limit.login.capacity=100",
        "rate-limit.login.refill-tokens=100",
        "rate-limit.login.refill-duration=1"
})
@AutoConfigureMockMvc
class SecurityRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private ScheduleRequestRepository scheduleRequestRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    private static final String TEST_EMAIL = "test@test.com";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String CLIENT_PASSWORD = "Pass12345";
    private static final String ADMIN_PASSWORD = "AdminPass123!";
    private static final String MANAGER_EMAIL = "manager@test.com";
    private static final String MANAGER_PASSWORD = "ManagerPass123!";

    private String validUserToken;
    private String validAdminToken;
    private String validManagerToken;
    private UUID testUserId;
    private UUID adminUserId;
    private UUID otherUserPaymentId;
    private UUID ownMembershipId;
    private UUID otherMembershipId;
    private UUID testPaymentId;
    private UUID testCheckoutId;
    private UUID ownScheduleRequestId;
    private UUID otherScheduleRequestId;

    @BeforeEach
    void setUp() {
        authService.unlockAccountLockout(ADMIN_EMAIL);

        User testUser = userRepository.findByEmail(TEST_EMAIL).orElseGet(() -> {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setName("Test User");
            user.setEmail(TEST_EMAIL);
            user.setPasswordHash(passwordEncoder.encode(CLIENT_PASSWORD));
            user.setRole(User.Role.CLIENT);
            user.setIsActive(true);
            return userRepository.save(user);
        });

        User adminUser = userRepository.findByEmail(ADMIN_EMAIL).orElseGet(() -> {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setName("Admin User");
            user.setEmail(ADMIN_EMAIL);
            user.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
            user.setRole(User.Role.ADMIN);
            user.setIsActive(true);
            return userRepository.save(user);
        });

        User managerUser = userRepository.findByEmail(MANAGER_EMAIL).orElseGet(() -> {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setName("Manager User");
            user.setEmail(MANAGER_EMAIL);
            user.setPasswordHash(passwordEncoder.encode(MANAGER_PASSWORD));
            user.setRole(User.Role.MANAGER);
            user.setIsActive(true);
            return userRepository.save(user);
        });

        User otherUser = userRepository.findByEmail("other@test.com").orElseGet(() -> {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setName("Other User");
            user.setEmail("other@test.com");
            user.setPasswordHash(passwordEncoder.encode("OtherPass123!"));
            user.setRole(User.Role.CLIENT);
            user.setIsActive(true);
            return userRepository.save(user);
        });

        User trainerUser = userRepository.findByEmail("trainer@test.com").orElseGet(() -> {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setName("Trainer User");
            user.setEmail("trainer@test.com");
            user.setPasswordHash(passwordEncoder.encode("TrainerPass123!"));
            user.setRole(User.Role.TRAINER);
            user.setIsActive(true);
            return userRepository.save(user);
        });

        Trainer trainer = trainerRepository.findByUserId(trainerUser.getId()).orElseGet(() -> trainerRepository.save(Trainer.builder()
                .user(trainerUser)
                .bio("Security test trainer")
                .specialties("MMA")
                .isActive(true)
                .build()));

        testUserId = testUser.getId();
        adminUserId = adminUser.getId();

        Plan plan = planRepository.findAll().stream().findFirst().orElseGet(() -> {
            Plan newPlan = Plan.builder()
                    .name("Security Test Plan")
                    .description("Security baseline plan")
                    .price(new BigDecimal("99.90"))
                    .currency("BRL")
                    .durationDays(30)
                    .isActive(true)
                    .build();
            return planRepository.save(newPlan);
        });

        Membership ownMembership = membershipRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(testUser.getId(), Membership.MembershipStatus.ACTIVE)
                .orElseGet(() -> membershipRepository.save(Membership.builder()
                        .user(testUser)
                        .plan(plan)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .status(Membership.MembershipStatus.ACTIVE)
                        .autoRenew(false)
                        .build()));
        ownMembershipId = ownMembership.getId();

        Membership otherMembership = membershipRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(otherUser.getId(), Membership.MembershipStatus.ACTIVE)
                .orElseGet(() -> membershipRepository.save(Membership.builder()
                        .user(otherUser)
                        .plan(plan)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .status(Membership.MembershipStatus.ACTIVE)
                        .autoRenew(false)
                        .build()));
        otherMembershipId = otherMembership.getId();

        Payment ownPayment = paymentRepository.findByUserId(testUser.getId(), org.springframework.data.domain.PageRequest.of(0, 1))
                .stream().findFirst().orElseGet(() -> paymentRepository.save(Payment.builder()
                        .user(testUser)
                        .membership(ownMembership)
                        .amount(new BigDecimal("50.00"))
                        .currency("BRL")
                        .method(Payment.PaymentMethod.CARD)
                        .status(Payment.PaymentStatus.PENDING)
                        .build()));

        Payment otherPayment = paymentRepository.findByUserId(otherUser.getId(), org.springframework.data.domain.PageRequest.of(0, 1))
                .stream().findFirst().orElseGet(() -> paymentRepository.save(Payment.builder()
                        .user(otherUser)
                        .membership(otherMembership)
                        .amount(new BigDecimal("75.00"))
                        .currency("BRL")
                        .method(Payment.PaymentMethod.CARD)
                        .status(Payment.PaymentStatus.PENDING)
                        .build()));

        ScheduleRequest ownScheduleRequest = scheduleRequestRepository.findByUserId(testUser.getId(), org.springframework.data.domain.PageRequest.of(0, 1))
                .stream().findFirst().orElseGet(() -> scheduleRequestRepository.save(ScheduleRequest.builder()
                        .user(testUser)
                        .trainer(trainer)
                        .preferredAt(LocalDateTime.now().plusDays(1))
                        .notes("Own request")
                        .status(ScheduleRequest.RequestStatus.PENDING)
                        .build()));

        ScheduleRequest otherScheduleRequest = scheduleRequestRepository.findByUserId(otherUser.getId(), org.springframework.data.domain.PageRequest.of(0, 1))
                .stream().findFirst().orElseGet(() -> scheduleRequestRepository.save(ScheduleRequest.builder()
                        .user(otherUser)
                        .trainer(trainer)
                        .preferredAt(LocalDateTime.now().plusDays(2))
                        .notes("Other request")
                        .status(ScheduleRequest.RequestStatus.PENDING)
                        .build()));

        validUserToken = jwtUtil.generateAccessToken(
                testUserId, TEST_EMAIL, "CLIENT"
        );
        validAdminToken = jwtUtil.generateAccessToken(
                adminUserId, "admin@test.com", "ADMIN"
        );
        validManagerToken = jwtUtil.generateAccessToken(
                managerUser.getId(), MANAGER_EMAIL, "MANAGER"
        );
        testPaymentId = ownPayment.getId();
        otherUserPaymentId = otherPayment.getId();
        testCheckoutId = UUID.randomUUID();
        ownScheduleRequestId = ownScheduleRequest.getId();
        otherScheduleRequestId = otherScheduleRequest.getId();
    }

    // ===== TEST 1: JWT Validation =====

    @Test
    @DisplayName("Invalid JWT token is rejected with 401")
    void invalidJwtTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/payments/" + testPaymentId)
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Missing JWT token is rejected with 401")
    void missingJwtTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/payments/" + testPaymentId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Valid JWT token is accepted (returns non-401)")
    void validJwtTokenIsAccepted() throws Exception {
        mockMvc.perform(get("/api/payments/" + testPaymentId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401) {
                        throw new AssertionError("Expected non-401 status, got " + status);
                    }
                });
    }

    // ===== TEST 2: Checkout Protection =====

    @Test
    @DisplayName("GET /api/checkout/{id}/status requires authentication - returns 401")
    void checkoutStatusRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/checkout/" + testCheckoutId + "/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/checkout/{id}/payment requires authentication - returns 401")
    void checkoutPaymentRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/checkout/" + testCheckoutId + "/payment"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/checkout endpoint responds without server error")
    void checkoutCreateIsPublic() throws Exception {
        String json = "{\"name\":\"Test\",\"email\":\"test@test.com\",\"password\":\"pass12345\",\"planId\":\"" + UUID.randomUUID() + "\",\"paymentMethod\":\"MBWAY\"}";
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status >= 500) {
                        throw new AssertionError("Expected non-5xx status, got " + status);
                    }
                });
    }

    // ===== TEST 3: Protected Endpoints =====

    @Test
    @DisplayName("Admin endpoints require authentication")
    void adminEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Manager endpoints require authentication")
    void managerEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/manager/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Pre-registration admin actions are forbidden for non-admin users")
    void preRegistrationAdminActionsRequireAdminRole() throws Exception {
        UUID leadId = UUID.randomUUID();
        mockMvc.perform(patch("/api/admin/pre-registrations/" + leadId + "/accept")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Client cannot complete payments manually")
    void clientCannotCompletePaymentManually() throws Exception {
        mockMvc.perform(patch("/api/payments/" + testPaymentId + "/complete")
                        .header("Authorization", "Bearer " + validUserToken)
                        .param("gatewayRef", "manual-test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Client cannot read another user's payment")
    void clientCannotReadAnotherUsersPayment() throws Exception {
        mockMvc.perform(get("/api/payments/" + otherUserPaymentId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Client cannot create payment for another user's membership")
    void clientCannotCreatePaymentForAnotherMembership() throws Exception {
        String json = "{\"membershipId\":\"" + otherMembershipId + "\",\"amount\":50.00,\"currency\":\"BRL\",\"method\":\"CARD\"}";
        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + validUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Client list endpoint returns only own payments")
    void clientListReturnsOnlyOwnPayments() throws Exception {
        mockMvc.perform(get("/api/payments")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(testUserId.toString())))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(otherUserPaymentId.toString()))));
    }

    @Test
    @DisplayName("Admin can access full membership listing")
    void adminCanAccessFullMembershipListing() throws Exception {
        mockMvc.perform(get("/api/memberships")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Manager can access full membership listing")
    void managerCanAccessFullMembershipListing() throws Exception {
        mockMvc.perform(get("/api/memberships")
                        .header("Authorization", "Bearer " + validManagerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Client cannot access full membership listing")
    void clientCannotAccessFullMembershipListing() throws Exception {
        mockMvc.perform(get("/api/memberships")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated cannot access full membership listing")
    void unauthenticatedCannotAccessFullMembershipListing() throws Exception {
        mockMvc.perform(get("/api/memberships"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Client can access own membership via me endpoint")
    void clientCanAccessOwnMembershipViaMeEndpoint() throws Exception {
        mockMvc.perform(get("/api/memberships/me")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(testUserId.toString())));
    }

    @Test
    @DisplayName("Client cannot access another user's membership by id")
    void clientCannotAccessAnotherUsersMembershipById() throws Exception {
        mockMvc.perform(get("/api/memberships/" + otherMembershipId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Client can access own membership by id")
    void clientCanAccessOwnMembershipById() throws Exception {
        mockMvc.perform(get("/api/memberships/" + ownMembershipId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(testUserId.toString())));
    }

    @Test
    @DisplayName("Admin can access another user's membership by id")
    void adminCanAccessAnotherUsersMembershipById() throws Exception {
        mockMvc.perform(get("/api/memberships/" + otherMembershipId)
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Manager cannot access another user's membership by id")
    void managerCannotAccessAnotherUsersMembershipById() throws Exception {
        mockMvc.perform(get("/api/memberships/" + otherMembershipId)
                        .header("Authorization", "Bearer " + validManagerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Unauthenticated cannot access membership by id")
    void unauthenticatedCannotAccessMembershipById() throws Exception {
        mockMvc.perform(get("/api/memberships/" + ownMembershipId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated cannot access schedule request by id")
    void unauthenticatedCannotAccessScheduleRequestById() throws Exception {
        mockMvc.perform(get("/api/schedule-requests/" + ownScheduleRequestId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Client can access own schedule request by id")
    void clientCanAccessOwnScheduleRequestById() throws Exception {
        mockMvc.perform(get("/api/schedule-requests/" + ownScheduleRequestId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(testUserId.toString())));
    }

    @Test
    @DisplayName("Client cannot access another user's schedule request by id")
    void clientCannotAccessAnotherUsersScheduleRequestById() throws Exception {
        mockMvc.perform(get("/api/schedule-requests/" + otherScheduleRequestId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Client cannot delete another user's schedule request")
    void clientCannotDeleteAnotherUsersScheduleRequest() throws Exception {
        mockMvc.perform(delete("/api/schedule-requests/" + otherScheduleRequestId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Admin can access another user's schedule request by id")
    void adminCanAccessAnotherUsersScheduleRequestById() throws Exception {
        mockMvc.perform(get("/api/schedule-requests/" + otherScheduleRequestId)
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Manager schedule-request list access remains allowed")
    void managerCanAccessScheduleRequestList() throws Exception {
        mockMvc.perform(get("/api/schedule-requests")
                        .header("Authorization", "Bearer " + validManagerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Client schedule-request list remains role-denied")
    void clientScheduleRequestListIsRoleDenied() throws Exception {
        mockMvc.perform(get("/api/schedule-requests")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isForbidden());
    }

    // ===== TEST 4: Endpoint Exists =====

    @Test
    @DisplayName("Rate limit endpoint exists and doesn't return 500")
    void rateLimitEndpointExists() throws Exception {
        String json = "{\"name\":\"Test\",\"email\":\"test@test.com\",\"password\":\"pass12345\",\"planId\":\"" + UUID.randomUUID() + "\",\"paymentMethod\":\"MBWAY\"}";
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 500 : "Endpoint should not return 500 internal error";
                });
    }

    // ===== TEST 5: Login / Refresh Security =====

    @Test
    @DisplayName("Wrong admin password returns 401 and does not set refresh cookie")
    void wrongAdminPasswordDoesNotAuthenticate() throws Exception {
        String loginJson = "{\"email\":\"" + ADMIN_EMAIL + "\",\"password\":\"wrong-password\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    @DisplayName("Locked account returns lockout message")
    void lockedAccountReturnsLockoutMessage() throws Exception {
        for (int i = 0; i < 5; i++) {
            String wrongPasswordJson = "{\"email\":\"" + ADMIN_EMAIL + "\",\"password\":\"wrong-password\"}";
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(wrongPasswordJson))
                    .andExpect(status().isUnauthorized());
        }

        String correctPasswordJson = "{\"email\":\"" + ADMIN_EMAIL + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctPasswordJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Account temporarily locked due to too many failed attempts")));
    }

    @Test
    @DisplayName("Unlock utility clears only lockout state")
    void unlockUtilityClearsOnlyLockoutState() throws Exception {
        for (int i = 0; i < 5; i++) {
            String wrongPasswordJson = "{\"email\":\"" + ADMIN_EMAIL + "\",\"password\":\"wrong-password\"}";
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(wrongPasswordJson))
                    .andExpect(status().isUnauthorized());
        }

        String correctPasswordJson = "{\"email\":\"" + ADMIN_EMAIL + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctPasswordJson))
                .andExpect(status().isUnauthorized());

        Assertions.assertTrue(authService.unlockAccountLockout("  " + ADMIN_EMAIL.toUpperCase() + "  "));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctPasswordJson))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String accessToken = extractField(body, "accessToken");
        Assertions.assertNotNull(accessToken, "accessToken must be present");
        Assertions.assertEquals("ADMIN", extractRole(accessToken));
    }

    @Test
    @DisplayName("Successful admin login returns ADMIN access token and refresh cookie")
    void successfulAdminLoginReturnsAdminTokenAndCookie() throws Exception {
        String loginJson = "{\"email\":\"" + ADMIN_EMAIL + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}";

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken=")))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String accessToken = extractField(body, "accessToken");
        Assertions.assertNotNull(accessToken, "accessToken must be present");
        Assertions.assertEquals("ADMIN", extractRole(accessToken));
    }

    @Test
    @DisplayName("Admin login accepts space-padded uppercase email and keeps ADMIN role")
    void adminLoginNormalizesEmailAndKeepsAdminRole() throws Exception {
        String loginJson = "{\"email\":\"  " + ADMIN_EMAIL.toUpperCase() + "  \",\"password\":\"" + ADMIN_PASSWORD + "\"}";

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String accessToken = extractField(body, "accessToken");
        Assertions.assertNotNull(accessToken, "accessToken must be present");
        Assertions.assertEquals("ADMIN", extractRole(accessToken));
    }

    @Test
    @DisplayName("Inactive user cannot login")
    void inactiveUserCannotLogin() throws Exception {
        String email = "inactive@test.com";
        userRepository.findByEmailIgnoreCase(email).ifPresent(userRepository::delete);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Inactive User");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("InactivePass123!"));
        user.setRole(User.Role.CLIENT);
        user.setIsActive(false);
        userRepository.save(user);

        String loginJson = "{\"email\":\"" + email + "\",\"password\":\"InactivePass123!\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Soft-deleted user cannot login")
    void deletedUserCannotLogin() throws Exception {
        String email = "deleted@test.com";
        userRepository.findByEmailIgnoreCase(email).ifPresent(userRepository::delete);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Deleted User");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("DeletedPass123!"));
        user.setRole(User.Role.CLIENT);
        user.setIsActive(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        String loginJson = "{\"email\":\"" + email + "\",\"password\":\"DeletedPass123!\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Old client refresh cookie can refresh after failed admin login attempt")
    void staleClientCookieStillRefreshesAfterFailedAdminLogin() throws Exception {
        String clientLoginJson = "{\"email\":\"" + TEST_EMAIL + "\",\"password\":\"" + CLIENT_PASSWORD + "\"}";
        MvcResult clientLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientLoginJson))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = clientLogin.getResponse().getHeader("Set-Cookie");
        Assertions.assertNotNull(setCookie, "client login must issue refresh cookie");
        MockCookie refreshCookie = new MockCookie("refreshToken", extractRefreshCookieValue(setCookie));

        String wrongAdminJson = "{\"email\":\"" + ADMIN_EMAIL + "\",\"password\":\"wrong-password\"}";
        mockMvc.perform(post("/api/auth/login")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongAdminJson))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"));

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String refreshBody = refreshResult.getResponse().getContentAsString();
        String refreshedAccessToken = extractField(refreshBody, "accessToken");
        Assertions.assertNotNull(refreshedAccessToken, "refresh must return accessToken");
        Assertions.assertEquals("CLIENT", extractRole(refreshedAccessToken));
    }

    @Test
    @DisplayName("Logout with valid auth revokes refresh token and refresh returns 401")
    void logoutWithAuthRevokesRefreshToken() throws Exception {
        String clientLoginJson = "{\"email\":\"" + TEST_EMAIL + "\",\"password\":\"" + CLIENT_PASSWORD + "\"}";
        MvcResult clientLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientLoginJson))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken=")))
                .andReturn();

        String setCookie = clientLogin.getResponse().getHeader("Set-Cookie");
        String body = clientLogin.getResponse().getContentAsString();
        String accessToken = extractField(body, "accessToken");
        Assertions.assertNotNull(setCookie, "client login must issue refresh cookie");
        Assertions.assertNotNull(accessToken, "client login must return access token");
        MockCookie refreshCookie = new MockCookie("refreshToken", extractRefreshCookieValue(setCookie));

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Logout without auth still clears cookie and returns safe response")
    void logoutWithoutAuthStillClearsCookie() throws Exception {
        MockCookie refreshCookie = new MockCookie("refreshToken", "stale-or-invalid-token");

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private static String extractField(String json, String fieldName) {
        String marker = "\"" + fieldName + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        int valueStart = start + marker.length();
        int valueEnd = json.indexOf('"', valueStart);
        if (valueEnd < 0) {
            return null;
        }
        return json.substring(valueStart, valueEnd);
    }

    private static String extractRole(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return extractField(payloadJson, "role");
    }

    private static String extractRefreshCookieValue(String setCookieHeader) {
        String[] parts = setCookieHeader.split(";", 2);
        String[] cookieParts = parts[0].split("=", 2);
        return cookieParts.length == 2 ? cookieParts[1] : "";
    }
}

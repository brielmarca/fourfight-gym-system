package com.gym.controller.security;

import com.gym.entity.User;
import com.gym.entity.Plan;
import com.gym.entity.Membership;
import com.gym.entity.Notification;
import com.gym.entity.Payment;
import com.gym.entity.ScheduleRequest;
import com.gym.entity.GymClass;
import com.gym.entity.Trainer;
import com.gym.repository.ClassEnrollmentRepository;
import com.gym.repository.ClassRepository;
import com.gym.repository.NotificationRepository;
import com.gym.repository.PaymentRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.MembershipRepository;
import com.gym.repository.ScheduleRequestRepository;
import com.gym.repository.TrainerRepository;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
import com.gym.security.RateLimitFilter;
import com.gym.service.AuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockCookie;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Base64;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
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
    private RateLimitFilter rateLimitFilter;

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

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassEnrollmentRepository classEnrollmentRepository;

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
    private UUID ownNotificationId;
    private UUID otherNotificationId;
    private UUID testClassId;
    private UUID trainerId;

    @BeforeEach
    void setUp() {
        rateLimitFilter.resetBuckets();
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

        Notification ownNotification = notificationRepository.findByUserId(testUser.getId(), org.springframework.data.domain.PageRequest.of(0, 1))
                .stream().findFirst().orElseGet(() -> notificationRepository.save(Notification.builder()
                        .user(testUser)
                        .title("Own notification")
                        .body("Own body")
                        .type(Notification.NotificationType.GENERAL)
                        .channel(Notification.NotificationChannel.IN_APP)
                        .build()));

        Notification otherNotification = notificationRepository.findByUserId(otherUser.getId(), org.springframework.data.domain.PageRequest.of(0, 1))
                .stream().findFirst().orElseGet(() -> notificationRepository.save(Notification.builder()
                        .user(otherUser)
                        .title("Other notification")
                        .body("Other body")
                        .type(Notification.NotificationType.GENERAL)
                        .channel(Notification.NotificationChannel.IN_APP)
                        .build()));

        trainerId = trainer.getId();
        GymClass testClass = classRepository.save(GymClass.builder()
                .trainer(trainer)
                .name("Security Test Class")
                .description("Class for security regression testing")
                .capacity(30)
                .schedule(java.time.LocalDateTime.now().plusDays(7))
                .durationMin(60)
                .status(GymClass.ClassStatus.SCHEDULED)
                .build());

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
        testClassId = testClass.getId();
        ownScheduleRequestId = ownScheduleRequest.getId();
        otherScheduleRequestId = otherScheduleRequest.getId();
        ownNotificationId = ownNotification.getId();
        otherNotificationId = otherNotification.getId();
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
    @DisplayName("Client can read own checkout status")
    void clientCanReadOwnCheckoutStatus() throws Exception {
        mockMvc.perform(get("/api/checkout/" + testPaymentId + "/status")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(testUserId.toString())));
    }

    @Test
    @DisplayName("Client cannot read another user's checkout status")
    void clientCannotReadAnotherUsersCheckoutStatus() throws Exception {
        mockMvc.perform(get("/api/checkout/" + otherUserPaymentId + "/status")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Admin can read another user's checkout status")
    void adminCanReadAnotherUsersCheckoutStatus() throws Exception {
        mockMvc.perform(get("/api/checkout/" + otherUserPaymentId + "/status")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(otherUserPaymentId.toString())));
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

    // ===== TEST 2B: Plan Mutation Protection =====

    @Test
    @DisplayName("Plan read endpoints remain public")
    void planReadEndpointsRemainPublic() throws Exception {
        UUID planId = firstPlanId();

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/plans/" + planId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unauthenticated user cannot mutate plans")
    void unauthenticatedCannotMutatePlans() throws Exception {
        UUID planId = firstPlanId();

        mockMvc.perform(post("/api/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlanJson("Unauth Plan")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/plans/" + planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePlanJson("Unauth Update")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/plans/" + planId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Client cannot mutate plans")
    void clientCannotMutatePlans() throws Exception {
        UUID planId = firstPlanId();

        mockMvc.perform(post("/api/plans")
                        .header("Authorization", "Bearer " + validUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlanJson("Client Plan")))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/plans/" + planId)
                        .header("Authorization", "Bearer " + validUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePlanJson("Client Update")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/plans/" + planId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can mutate plans")
    void adminCanMutatePlans() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/plans")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlanJson("Admin Security Plan")))
                .andExpect(status().isCreated())
                .andReturn();
        UUID createdPlanId = UUID.fromString(extractField(createResult.getResponse().getContentAsString(), "id"));

        mockMvc.perform(put("/api/plans/" + createdPlanId)
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePlanJson("Admin Security Plan Updated")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Admin Security Plan Updated")));

        mockMvc.perform(delete("/api/plans/" + createdPlanId)
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isNoContent());
    }

    // ===== TEST 3: Protected Endpoints =====

    @Test
    @DisplayName("Admin endpoints require authentication")
    void adminEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/admin/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CLIENT cannot access admin student listing")
    void clientCannotAccessAdminStudentListing() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Registered CLIENT without membership appears in admin student listing")
    @Sql(statements = "CREATE TABLE IF NOT EXISTS pre_registration_profile_days (profile_id UUID NOT NULL, \"day\" VARCHAR(20) NOT NULL, PRIMARY KEY (profile_id, \"day\"))")
    void registeredClientWithoutMembershipAppearsInAdminStudentListing() throws Exception {
        String email = "registered-" + UUID.randomUUID() + "@test.com";
        LocalDate dob = LocalDate.now().minusYears(24).minusDays(2);
        int age = Period.between(dob, LocalDate.now()).getYears();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email, dob, age)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"role\":\"CLIENT\"")));

        mockMvc.perform(get("/api/admin/students?size=100")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(email)))
                .andExpect(content().string(containsString("\"status\":\"REGISTERED\"")));

        mockMvc.perform(get("/api/admin/students?size=100")
                        .header("Authorization", "Bearer " + validManagerToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(email)));
    }

    @Test
    @DisplayName("Admin student listing defaults to newest clients first")
    void adminStudentListingDefaultsToNewestClientsFirst() throws Exception {
        String olderEmail = "older-admin-student-" + UUID.randomUUID() + "@test.com";
        String newestEmail = "newest-admin-student-" + UUID.randomUUID() + "@test.com";

        userRepository.save(User.builder()
                .name("Older Admin Student")
                .email(olderEmail)
                .passwordHash(passwordEncoder.encode("OlderPass123!"))
                .role(User.Role.CLIENT)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build());

        userRepository.save(User.builder()
                .name("Newest Admin Student")
                .email(newestEmail)
                .passwordHash(passwordEncoder.encode("NewestPass123!"))
                .role(User.Role.CLIENT)
                .isActive(true)
                .createdAt(LocalDateTime.now().plusDays(2))
                .updatedAt(LocalDateTime.now().plusDays(2))
                .build());

        mockMvc.perform(get("/api/admin/students?size=1")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userEmail").value(newestEmail))
                .andExpect(jsonPath("$.content[0].status").value("REGISTERED"));
    }

    @Test
    @DisplayName("Unauthenticated user cannot deactivate student")
    void unauthenticatedCannotDeactivateStudent() throws Exception {
        User student = createTestUser("deactivate-unauth-" + UUID.randomUUID() + "@test.com", User.Role.CLIENT, true);

        mockMvc.perform(post("/api/admin/students/" + student.getId() + "/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson("No longer active")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CLIENT cannot deactivate student")
    void clientCannotDeactivateStudent() throws Exception {
        User student = createTestUser("deactivate-client-" + UUID.randomUUID() + "@test.com", User.Role.CLIENT, true);

        mockMvc.perform(post("/api/admin/students/" + student.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + validUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson("No longer active")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MANAGER cannot deactivate student")
    void managerCannotDeactivateStudent() throws Exception {
        User student = createTestUser("deactivate-manager-" + UUID.randomUUID() + "@test.com", User.Role.CLIENT, true);

        mockMvc.perform(post("/api/admin/students/" + student.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + validManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson("No longer active")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can deactivate CLIENT with required reason and preserve history")
    void adminCanDeactivateClientWithReasonAndPreserveHistory() throws Exception {
        User student = createTestUser("deactivate-admin-" + UUID.randomUUID() + "@test.com", User.Role.CLIENT, true);
        Plan plan = planRepository.findById(firstPlanId()).orElseThrow();
        Membership membership = membershipRepository.save(Membership.builder()
                .user(student)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(Membership.MembershipStatus.ACTIVE)
                .autoRenew(false)
                .build());
        Payment payment = paymentRepository.save(Payment.builder()
                .user(student)
                .membership(membership)
                .amount(new BigDecimal("50.00"))
                .currency("BRL")
                .method(Payment.PaymentMethod.CARD)
                .status(Payment.PaymentStatus.PENDING)
                .build());

        mockMvc.perform(post("/api/admin/students/" + student.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson("  Repeated bad behavior  ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(student.getId().toString()))
                .andExpect(jsonPath("$.email").value(student.getEmail()))
                .andExpect(jsonPath("$.name").value(student.getName()))
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.deactivatedBy").value(adminUserId.toString()))
                .andExpect(jsonPath("$.deactivationReason").value("Repeated bad behavior"));

        User deactivated = userRepository.findById(student.getId()).orElseThrow();
        Assertions.assertFalse(deactivated.getIsActive());
        Assertions.assertNotNull(deactivated.getDeactivatedAt());
        Assertions.assertEquals(adminUserId, deactivated.getDeactivatedBy().getId());
        Assertions.assertEquals("Repeated bad behavior", deactivated.getDeactivationReason());
        Assertions.assertNull(deactivated.getDeletedAt());
        Assertions.assertTrue(membershipRepository.existsById(membership.getId()));
        Assertions.assertTrue(paymentRepository.existsById(payment.getId()));
    }

    @Test
    @DisplayName("Deactivation reason is required")
    void deactivationReasonIsRequired() throws Exception {
        User student = createTestUser("deactivate-reason-" + UUID.randomUUID() + "@test.com", User.Role.CLIENT, true);

        mockMvc.perform(post("/api/admin/students/" + student.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson("   ")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Admin cannot deactivate self")
    void adminCannotDeactivateSelf() throws Exception {
        mockMvc.perform(post("/api/admin/students/" + adminUserId + "/deactivate")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson("Self deactivate")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Admin cannot deactivate another ADMIN")
    void adminCannotDeactivateAnotherAdmin() throws Exception {
        User otherAdmin = createTestUser("other-admin-" + UUID.randomUUID() + "@test.com", User.Role.ADMIN, true);

        mockMvc.perform(post("/api/admin/students/" + otherAdmin.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson("Not allowed")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Repeated deactivation returns clear validation error")
    void repeatedDeactivationReturnsClearValidationError() throws Exception {
        User student = createTestUser("deactivate-repeat-" + UUID.randomUUID() + "@test.com", User.Role.CLIENT, true);

        mockMvc.perform(post("/api/admin/students/" + student.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson("First reason")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/students/" + student.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson("Second reason")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("already inactive")));
    }

    @Test
    @DisplayName("Deactivated user cannot use old access token or refresh session")
    void deactivatedUserCannotUseOldAccessTokenOrRefreshSession() throws Exception {
        String email = "deactivate-session-" + UUID.randomUUID() + "@test.com";
        createTestUser(email, User.Role.CLIENT, true);

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"TestPass123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = extractField(login.getResponse().getContentAsString(), "accessToken");
        String setCookie = login.getResponse().getHeader("Set-Cookie");
        Assertions.assertNotNull(accessToken);
        Assertions.assertNotNull(setCookie);
        MockCookie refreshCookie = new MockCookie("refreshToken", extractRefreshCookieValue(setCookie));
        User student = userRepository.findByEmailIgnoreCase(email).orElseThrow();

        mockMvc.perform(post("/api/admin/students/" + student.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson("Session revocation")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON))
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
    @DisplayName("Client cannot create arbitrary membership")
    void clientCannotCreateArbitraryMembership() throws Exception {
        String json = createMembershipJson(testUserId, firstPlanId());

        mockMvc.perform(post("/api/memberships")
                        .header("Authorization", "Bearer " + validUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can create membership through existing admin operation")
    void adminCanCreateMembership() throws Exception {
        String json = createMembershipJson(testUserId, firstPlanId());

        mockMvc.perform(post("/api/memberships")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(testUserId.toString())));
    }

    @Test
    @DisplayName("Manager can create membership through existing manager operation")
    void managerCanCreateMembership() throws Exception {
        String json = createMembershipJson(testUserId, firstPlanId());

        mockMvc.perform(post("/api/memberships")
                        .header("Authorization", "Bearer " + validManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(testUserId.toString())));
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

    @Test
    @DisplayName("Unauthenticated cannot access notification by id")
    void unauthenticatedCannotAccessNotificationById() throws Exception {
        mockMvc.perform(get("/api/notifications/" + ownNotificationId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Client can access own notification by id")
    void clientCanAccessOwnNotificationById() throws Exception {
        mockMvc.perform(get("/api/notifications/" + ownNotificationId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(testUserId.toString())));
    }

    @Test
    @DisplayName("Client cannot access another user's notification by id")
    void clientCannotAccessAnotherUsersNotificationById() throws Exception {
        mockMvc.perform(get("/api/notifications/" + otherNotificationId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Client cannot mark another user's notification as read")
    void clientCannotMarkAnotherUsersNotificationAsRead() throws Exception {
        mockMvc.perform(patch("/api/notifications/" + otherNotificationId + "/read")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isNotFound());
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

    // ===== CLASS ENDPOINT AUTHORIZATION TESTS =====

    @Test
    @DisplayName("Unauthenticated user cannot list classes")
    void unauthenticatedCannotListClasses() throws Exception {
        mockMvc.perform(get("/api/classes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated user cannot get class by id")
    void unauthenticatedCannotGetClassById() throws Exception {
        mockMvc.perform(get("/api/classes/" + testClassId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated user cannot create class")
    void unauthenticatedCannotCreateClass() throws Exception {
        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Unauth Class\",\"capacity\":20,\"schedule\":\"2026-07-01T10:00:00\",\"durationMin\":60}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated user cannot update class")
    void unauthenticatedCannotUpdateClass() throws Exception {
        mockMvc.perform(put("/api/classes/" + testClassId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hacked Name\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated user cannot delete class")
    void unauthenticatedCannotDeleteClass() throws Exception {
        mockMvc.perform(delete("/api/classes/" + testClassId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated user cannot enroll")
    void unauthenticatedCannotEnroll() throws Exception {
        mockMvc.perform(post("/api/classes/" + testClassId + "/enroll"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated user cannot unenroll")
    void unauthenticatedCannotUnenroll() throws Exception {
        mockMvc.perform(post("/api/classes/" + testClassId + "/unenroll"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated user cannot access roster")
    void unauthenticatedCannotAccessRoster() throws Exception {
        mockMvc.perform(get("/api/classes/" + testClassId + "/roster"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Client can view class list")
    void clientCanViewClassList() throws Exception {
        mockMvc.perform(get("/api/classes")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Client can view class detail")
    void clientCanViewClassDetail() throws Exception {
        mockMvc.perform(get("/api/classes/" + testClassId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Client cannot create class")
    void clientCannotCreateClass() throws Exception {
        mockMvc.perform(post("/api/classes")
                        .header("Authorization", "Bearer " + validUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Client Class\",\"trainerId\":\"" + UUID.randomUUID() + "\",\"capacity\":20,\"schedule\":\"2026-07-01T10:00:00\",\"durationMin\":60}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Client cannot update class")
    void clientCannotUpdateClass() throws Exception {
        mockMvc.perform(put("/api/classes/" + testClassId)
                        .header("Authorization", "Bearer " + validUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Client Updated\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Client cannot delete class")
    void clientCannotDeleteClass() throws Exception {
        mockMvc.perform(delete("/api/classes/" + testClassId)
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Client cannot access roster")
    void clientCannotAccessRoster() throws Exception {
        mockMvc.perform(get("/api/classes/" + testClassId + "/roster")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Client can enroll self")
    void clientCanEnrollSelf() throws Exception {
        mockMvc.perform(post("/api/classes/" + testClassId + "/enroll")
                        .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isCreated());
        // Clean up
        mockMvc.perform(post("/api/classes/" + testClassId + "/unenroll")
                        .header("Authorization", "Bearer " + validUserToken));
    }

    @Test
    @DisplayName("Admin can create, update, and delete class")
    void adminCanManageClasses() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/classes")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Admin Test Class\",\"trainerId\":\"" + trainerId + "\",\"capacity\":25,\"schedule\":\"2026-08-01T10:00:00\",\"durationMin\":60}"))
                .andExpect(status().isCreated())
                .andReturn();
        UUID createdClassId = UUID.fromString(extractField(createResult.getResponse().getContentAsString(), "id"));

        mockMvc.perform(put("/api/classes/" + createdClassId)
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Admin Updated Class\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/classes/" + createdClassId)
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Manager can create, update, and delete class")
    void managerCanManageClasses() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/classes")
                        .header("Authorization", "Bearer " + validManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Manager Test Class\",\"trainerId\":\"" + trainerId + "\",\"capacity\":25,\"schedule\":\"2026-08-01T14:00:00\",\"durationMin\":60}"))
                .andExpect(status().isCreated())
                .andReturn();
        UUID createdClassId = UUID.fromString(extractField(createResult.getResponse().getContentAsString(), "id"));

        mockMvc.perform(put("/api/classes/" + createdClassId)
                        .header("Authorization", "Bearer " + validManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Manager Updated Class\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/classes/" + createdClassId)
                        .header("Authorization", "Bearer " + validManagerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Admin can access roster")
    void adminCanAccessRoster() throws Exception {
        mockMvc.perform(get("/api/classes/" + testClassId + "/roster")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Manager can access roster")
    void managerCanAccessRoster() throws Exception {
        mockMvc.perform(get("/api/classes/" + testClassId + "/roster")
                        .header("Authorization", "Bearer " + validManagerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public programs endpoint remains accessible")
    void publicProgramsEndpointRemainsAccessible() throws Exception {
        mockMvc.perform(get("/api/programs"))
                .andExpect(status().isOk());
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

    private static String registerJson(String email, LocalDate dateOfBirth, int age) {
        return "{" +
                "\"name\":\"Registered Client\"," +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"RegisterPass123!\"," +
                "\"phone\":\"+351912345679\"," +
                "\"dateOfBirth\":\"" + dateOfBirth + "\"," +
                "\"age\":" + age + "," +
                "\"parishOrArea\":\"Lisboa\"," +
                "\"hasMartialArtsExperience\":false," +
                "\"martialArtsExperienceDetails\":null," +
                "\"trainingGoal\":\"Melhorar condicionamento\"," +
                "\"preferredModality\":\"JIU_JITSU\"," +
                "\"preferredModalityOther\":null," +
                "\"preferredTrainingTime\":\"NIGHT_AFTER_18\"," +
                "\"preferredTrainingTimeOther\":null," +
                "\"preferredTrainingDays\":[\"MONDAY\"]," +
                "\"valuesMartialArtsPhilosophy\":true," +
                "\"preferredContactMethod\":\"MESSAGE\"," +
                "\"preferredContactMethodOther\":null" +
                "}";
    }

    private UUID firstPlanId() {
        return planRepository.findAll().stream()
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private static String createPlanJson(String name) {
        return "{" +
                "\"name\":\"" + name + "\"," +
                "\"description\":\"Security regression plan\"," +
                "\"price\":49.90," +
                "\"durationDays\":30," +
                "\"maxClasses\":12," +
                "\"features\":[\"Security test\"]," +
                "\"level\":\"all\"," +
                "\"instructor\":\"Test\"," +
                "\"schedule\":[\"Monday\"]" +
                "}";
    }

    private static String updatePlanJson(String name) {
        return "{" +
                "\"name\":\"" + name + "\"," +
                "\"description\":\"Updated security regression plan\"," +
                "\"price\":59.90," +
                "\"durationDays\":30," +
                "\"maxClasses\":16," +
                "\"isActive\":true" +
                "}";
    }

    private static String createMembershipJson(UUID userId, UUID planId) {
        return "{" +
                "\"userId\":\"" + userId + "\"," +
                "\"planId\":\"" + planId + "\"," +
                "\"startDate\":\"" + LocalDate.now() + "\"," +
                "\"autoRenew\":false" +
                "}";
    }

    private User createTestUser(String email, User.Role role, boolean active) {
        return userRepository.save(User.builder()
                .name("Lifecycle Test User")
                .email(email)
                .passwordHash(passwordEncoder.encode("TestPass123!"))
                .role(role)
                .isActive(active)
                .build());
    }

    private static String deactivateJson(String reason) {
        return "{\"reason\":\"" + reason + "\"}";
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

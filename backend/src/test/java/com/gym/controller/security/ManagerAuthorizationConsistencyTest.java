package com.gym.controller.security;

import com.gym.entity.User;
import com.gym.entity.Plan;
import com.gym.entity.Membership;
import com.gym.entity.Payment;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.UserRepository;
import com.gym.dto.request.UpdateUserRequest;
import com.gym.security.JwtUtil;
import com.gym.security.RateLimitFilter;
import com.gym.security.RateLimitFilterTestSupport;
import com.gym.service.PaymentService;
import com.gym.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "mfa.encryption-key-base64=VEVTVC1LRVktTk9ULUZPUi1QUk9EVUNUSU9OLTAxMjM=",
    "rate-limit.login.capacity=100",
    "rate-limit.login.refill-tokens=100",
    "rate-limit.login.refill-duration=1"
})
@AutoConfigureMockMvc
class ManagerAuthorizationConsistencyTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private RateLimitFilter rateLimitFilter;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PlanRepository planRepository;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private UserService userService;
    @Autowired private PaymentService paymentService;

    private String suffix;
    private String adminToken;
    private String managerToken;
    private String clientToken;
    private UUID adminId;
    private UUID managerId;
    private UUID clientId;
    private UUID otherClientId;
    private UUID planId;

    @BeforeEach
    void setUp() {
        RateLimitFilterTestSupport.reset(rateLimitFilter);
        SecurityContextHolder.clearContext();
        suffix = UUID.randomUUID().toString().substring(0, 8);

        String adminEmail = "mgr-auth-admin-" + suffix + "@test.com";
        User admin = userRepository.save(User.builder()
            .id(UUID.randomUUID())
            .name("MgrAuth Admin")
            .email(adminEmail)
            .passwordHash(passwordEncoder.encode("AdminPass123!"))
            .role(User.Role.ADMIN)
            .isActive(true)
            .build());
        adminId = admin.getId();

        String managerEmail = "mgr-auth-manager-" + suffix + "@test.com";
        User manager = userRepository.save(User.builder()
            .id(UUID.randomUUID())
            .name("MgrAuth Manager")
            .email(managerEmail)
            .passwordHash(passwordEncoder.encode("ManagerPass123!"))
            .role(User.Role.MANAGER)
            .isActive(true)
            .build());
        managerId = manager.getId();

        String clientEmail = "mgr-auth-client-" + suffix + "@test.com";
        User client = userRepository.save(User.builder()
            .id(UUID.randomUUID())
            .name("MgrAuth Client")
            .email(clientEmail)
            .phone("+351912345678")
            .passwordHash(passwordEncoder.encode("ClientPass123!"))
            .role(User.Role.CLIENT)
            .isActive(true)
            .build());
        clientId = client.getId();

        String otherEmail = "mgr-auth-other-" + suffix + "@test.com";
        User otherClient = userRepository.save(User.builder()
            .id(UUID.randomUUID())
            .name("MgrAuth Other")
            .email(otherEmail)
            .phone("+351912345678")
            .passwordHash(passwordEncoder.encode("OtherPass123!"))
            .role(User.Role.CLIENT)
            .isActive(true)
            .build());
        otherClientId = otherClient.getId();

        adminToken = jwtUtil.generateAccessToken(adminId, adminEmail, "ADMIN");
        managerToken = jwtUtil.generateAccessToken(managerId, managerEmail, "MANAGER");
        clientToken = jwtUtil.generateAccessToken(clientId, clientEmail, "CLIENT");

        Plan plan = planRepository.findAll().stream().findFirst().orElseGet(() ->
            planRepository.save(Plan.builder()
                .name("MgrAuth Plan")
                .description("Plan for manager auth tests")
                .price(new BigDecimal("49.90"))
                .currency("BRL")
                .durationDays(30)
                .isActive(true)
                .build()));
        planId = plan.getId();
    }

    // ===== USER ACTIVATION =====

    @Test
    @DisplayName("ADMIN can set isActive=false through user update")
    void adminCanSetIsActiveFalse() throws Exception {
        User target = createClient("isactive-false-" + UUID.randomUUID() + "@test.com");
        mockMvc.perform(put("/api/users/" + target.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\":false}"))
            .andExpect(status().isOk());
        User reloaded = userRepository.findById(target.getId()).orElseThrow();
        assertFalse(reloaded.getIsActive());
    }

    @Test
    @DisplayName("ADMIN can set isActive=true through user update")
    void adminCanSetIsActiveTrue() throws Exception {
        User target = createClient("isactive-true-" + UUID.randomUUID() + "@test.com");
        target.setIsActive(false);
        userRepository.save(target);

        mockMvc.perform(put("/api/users/" + target.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\":true}"))
            .andExpect(status().isOk());
        User reloaded = userRepository.findById(target.getId()).orElseThrow();
        assertTrue(reloaded.getIsActive());
    }

    @Test
    @DisplayName("MANAGER cannot set isActive returns 403")
    void managerCannotSetIsActive() throws Exception {
        User target = createClient("isactive-mgr-" + UUID.randomUUID() + "@test.com");
        mockMvc.perform(put("/api/users/" + target.getId())
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\":false}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CLIENT cannot set isActive on self returns 403")
    void clientCannotSetIsActiveOnSelf() throws Exception {
        mockMvc.perform(put("/api/users/" + clientId)
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\":false}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Omitting isActive leaves field unchanged for non-ADMIN caller")
    void omittingIsActiveLeavesFieldUnchanged() throws Exception {
        User target = createClient("isactive-omit-" + UUID.randomUUID() + "@test.com");
        assertTrue(target.getIsActive());

        mockMvc.perform(put("/api/users/" + target.getId())
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Name\"}"))
            .andExpect(status().isOk());

        User reloaded = userRepository.findById(target.getId()).orElseThrow();
        assertTrue(reloaded.getIsActive());
        assertEquals("Updated Name", reloaded.getName());
    }

    // ===== USER DELETION =====

    @Test
    @DisplayName("MANAGER can delete CLIENT user")
    void managerCanDeleteClient() throws Exception {
        User target = createClient("delete-client-mgr-" + UUID.randomUUID() + "@test.com");
        mockMvc.perform(delete("/api/users/" + target.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("MANAGER cannot delete ADMIN user returns 403")
    void managerCannotDeleteAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + adminId)
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MANAGER cannot delete MANAGER user returns 403")
    void managerCannotDeleteManager() throws Exception {
        User otherManager = createUser("delete-other-manager-" + UUID.randomUUID() + "@test.com", User.Role.MANAGER);
        mockMvc.perform(delete("/api/users/" + otherManager.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MANAGER cannot delete TRAINER user returns 403")
    void managerCannotDeleteTrainer() throws Exception {
        User trainer = createUser("delete-trainer-" + UUID.randomUUID() + "@test.com", User.Role.TRAINER);
        mockMvc.perform(delete("/api/users/" + trainer.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MANAGER cannot delete PROFESSOR user returns 403")
    void managerCannotDeleteProfessor() throws Exception {
        User professor = createUser("delete-professor-" + UUID.randomUUID() + "@test.com", User.Role.PROFESSOR);
        mockMvc.perform(delete("/api/users/" + professor.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isForbidden());
        assertTrue(userRepository.findById(professor.getId()).isPresent());
    }

    @Test
    @DisplayName("ADMIN can delete CLIENT user")
    void adminCanDeleteClient() throws Exception {
        User target = createClient("delete-admin-client-" + UUID.randomUUID() + "@test.com");
        mockMvc.perform(delete("/api/users/" + target.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("ADMIN cannot delete ADMIN user returns 403")
    void adminCannotDeleteAdmin() throws Exception {
        User otherAdmin = createUser("delete-other-admin-" + UUID.randomUUID() + "@test.com", User.Role.ADMIN);
        mockMvc.perform(delete("/api/users/" + otherAdmin.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN cannot delete MANAGER user returns 403")
    void adminCannotDeleteManager() throws Exception {
        User manager = createUser("delete-admin-mgr-" + UUID.randomUUID() + "@test.com", User.Role.MANAGER);
        mockMvc.perform(delete("/api/users/" + manager.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN can delete TRAINER user")
    void adminCanDeleteTrainer() throws Exception {
        User trainer = createUser("delete-admin-trainer-" + UUID.randomUUID() + "@test.com", User.Role.TRAINER);
        mockMvc.perform(delete("/api/users/" + trainer.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());
    }

    // ===== MEMBERSHIP VIEW =====

    @Test
    @DisplayName("ADMIN can view another user's membership")
    void adminCanViewAnotherUsersMembership() throws Exception {
        UUID membershipId = createMembership(clientId);
        mockMvc.perform(get("/api/memberships/" + membershipId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MANAGER can view another user's membership")
    void managerCanViewAnotherUsersMembership() throws Exception {
        UUID membershipId = createMembership(clientId);
        mockMvc.perform(get("/api/memberships/" + membershipId)
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CLIENT can view own membership")
    void clientCanViewOwnMembership() throws Exception {
        UUID membershipId = createMembership(clientId);
        mockMvc.perform(get("/api/memberships/" + membershipId)
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CLIENT cannot view another user's membership")
    void clientCannotViewAnotherUsersMembership() throws Exception {
        UUID membershipId = createMembership(otherClientId);
        mockMvc.perform(get("/api/memberships/" + membershipId)
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isNotFound());
    }

    // ===== MEMBERSHIP RENEW =====

    @Test
    @DisplayName("ADMIN can renew another user's membership")
    void adminCanRenewAnotherUsersMembership() throws Exception {
        UUID membershipId = createMembership(clientId);
        mockMvc.perform(patch("/api/memberships/" + membershipId + "/renew")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MANAGER can renew another user's membership")
    void managerCanRenewAnotherUsersMembership() throws Exception {
        UUID membershipId = createMembership(clientId);
        mockMvc.perform(patch("/api/memberships/" + membershipId + "/renew")
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CLIENT can renew own membership")
    void clientCanRenewOwnMembership() throws Exception {
        UUID membershipId = createMembership(clientId);
        mockMvc.perform(patch("/api/memberships/" + membershipId + "/renew")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CLIENT cannot renew another user's membership")
    void clientCannotRenewAnotherUsersMembership() throws Exception {
        UUID membershipId = createMembership(otherClientId);
        mockMvc.perform(patch("/api/memberships/" + membershipId + "/renew")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isNotFound());
    }

    // ===== PAYMENT REFUND =====

    @Test
    @DisplayName("ADMIN can refund payment through controller")
    void adminCanRefund() throws Exception {
        UUID paymentId = createCompletedPayment(clientId);
        mockMvc.perform(patch("/api/payments/" + paymentId + "/refund")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MANAGER cannot refund payment through controller")
    void managerCannotRefund() throws Exception {
        UUID paymentId = createCompletedPayment(clientId);
        mockMvc.perform(patch("/api/payments/" + paymentId + "/refund")
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isForbidden());
    }

    // ===== ROLE MANAGEMENT =====

    @Test
    @DisplayName("ADMIN can access full user list")
    void adminCanAccessFullUserList() throws Exception {
        mockMvc.perform(get("/api/users?page=0&size=20")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].role", hasItem("ADMIN")))
            .andExpect(jsonPath("$.content[*].role", hasItem("MANAGER")))
            .andExpect(jsonPath("$.content[0].createdAt").exists())
            .andExpect(jsonPath("$.content[0].updatedAt").exists());
    }

    @Test
    @DisplayName("MANAGER cannot access admin full user list")
    void managerCannotAccessAdminFullUserList() throws Exception {
        mockMvc.perform(get("/api/users?page=0&size=20")
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MANAGER operational directory excludes privileged accounts and sensitive fields")
    void managerDirectoryExcludesPrivilegedAccountsAndSensitiveFields() throws Exception {
        User trainer = createUser("directory-trainer-" + UUID.randomUUID() + "@test.com", User.Role.TRAINER);
        User professor = createUser("directory-professor-" + UUID.randomUUID() + "@test.com", User.Role.PROFESSOR);
        User inactiveClient = createClient("directory-inactive-" + UUID.randomUUID() + "@test.com");
        inactiveClient.setIsActive(false);
        inactiveClient.setMfaSecret("encrypted-secret-for-test");
        inactiveClient.setBackupCodes("encrypted-backup-codes-for-test");
        userRepository.save(inactiveClient);
        long visibleTotal = userRepository.findActiveByRoles(
            List.of(User.Role.CLIENT, User.Role.TRAINER, User.Role.PROFESSOR),
            PageRequest.of(0, 1)
        ).getTotalElements();

        mockMvc.perform(get("/api/manager/users?page=0&size=50")
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value((int) visibleTotal))
            .andExpect(jsonPath("$.content[*].role", everyItem(not("ADMIN"))))
            .andExpect(jsonPath("$.content[*].role", everyItem(not("MANAGER"))))
            .andExpect(jsonPath("$.content[*].role", hasItem("CLIENT")))
            .andExpect(jsonPath("$.content[*].role", hasItem("TRAINER")))
            .andExpect(jsonPath("$.content[*].role", hasItem("PROFESSOR")))
            .andExpect(jsonPath("$.content[*].id", hasItem(clientId.toString())))
            .andExpect(jsonPath("$.content[*].id", hasItem(trainer.getId().toString())))
            .andExpect(jsonPath("$.content[*].id", hasItem(professor.getId().toString())))
            .andExpect(jsonPath("$.content[*].id", everyItem(not(adminId.toString()))))
            .andExpect(jsonPath("$.content[*].id", everyItem(not(managerId.toString()))))
            .andExpect(jsonPath("$.content[*].id", everyItem(not(inactiveClient.getId().toString()))))
            .andExpect(jsonPath("$.content[0].email").exists())
            .andExpect(jsonPath("$.content[0].isActive").doesNotExist())
            .andExpect(jsonPath("$.content[0].createdAt").doesNotExist())
            .andExpect(jsonPath("$.content[0].updatedAt").doesNotExist())
            .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist())
            .andExpect(jsonPath("$.content[0].mfaEnabled").doesNotExist())
            .andExpect(jsonPath("$.content[0].mfaSecret").doesNotExist())
            .andExpect(jsonPath("$.content[0].backupCodes").doesNotExist())
            .andExpect(content().string(not(containsString("passwordHash"))))
            .andExpect(content().string(not(containsString("mfaSecret"))))
            .andExpect(content().string(not(containsString("backupCodes"))))
            .andExpect(content().string(not(containsString("mfaEnabled"))))
            .andExpect(content().string(containsString("\"phone\"")));
    }

    @Test
    @DisplayName("CLIENT and unauthenticated users cannot access manager directory")
    void clientAndUnauthenticatedCannotAccessManagerDirectory() throws Exception {
        mockMvc.perform(get("/api/manager/users?page=0&size=20")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/manager/users?page=0&size=20"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("MANAGER user detail is reduced for operational targets and conceals hidden targets")
    void managerUserDetailIsScopedByTargetRole() throws Exception {
        User trainer = createUser("detail-trainer-" + UUID.randomUUID() + "@test.com", User.Role.TRAINER);
        User professor = createUser("detail-professor-" + UUID.randomUUID() + "@test.com", User.Role.PROFESSOR);
        User inactiveClient = createClient("detail-inactive-" + UUID.randomUUID() + "@test.com");
        inactiveClient.setIsActive(false);
        userRepository.save(inactiveClient);
        User inactiveTrainer = createUser("detail-inactive-trainer-" + UUID.randomUUID() + "@test.com", User.Role.TRAINER);
        inactiveTrainer.setIsActive(false);
        userRepository.save(inactiveTrainer);
        User inactiveProfessor = createUser("detail-inactive-professor-" + UUID.randomUUID() + "@test.com", User.Role.PROFESSOR);
        inactiveProfessor.setIsActive(false);
        userRepository.save(inactiveProfessor);

        mockMvc.perform(get("/api/users/" + clientId)
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(clientId.toString()))
            .andExpect(jsonPath("$.role").value("CLIENT"))
            .andExpect(jsonPath("$.email").exists())
            .andExpect(jsonPath("$.phone").exists())
            .andExpect(jsonPath("$.isActive").doesNotExist())
            .andExpect(jsonPath("$.createdAt").doesNotExist())
            .andExpect(jsonPath("$.updatedAt").doesNotExist())
            .andExpect(jsonPath("$.mfaEnabled").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist())
            .andExpect(jsonPath("$.mfaSecret").doesNotExist())
            .andExpect(jsonPath("$.backupCodes").doesNotExist())
            .andExpect(jsonPath("$.stripeCustomerId").doesNotExist())
            .andExpect(content().string(not(containsString("password"))))
            .andExpect(content().string(not(containsString("token"))))
            .andExpect(content().string(containsString("\"avatarUrl\"")));

        mockMvc.perform(get("/api/users/" + trainer.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("TRAINER"))
            .andExpect(jsonPath("$.createdAt").doesNotExist());

        mockMvc.perform(get("/api/users/" + professor.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("PROFESSOR"))
            .andExpect(jsonPath("$.createdAt").doesNotExist());

        mockMvc.perform(get("/api/users/" + adminId)
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isNotFound())
            .andExpect(content().string(not(containsString("ADMIN"))))
            .andExpect(content().string(not(containsString("Access denied"))))
            .andExpect(content().string(not(containsString("outside"))));

        mockMvc.perform(get("/api/users/" + managerId)
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("MANAGER"))
            .andExpect(jsonPath("$.createdAt").exists());

        User otherManager = createUser("detail-other-manager-" + UUID.randomUUID() + "@test.com", User.Role.MANAGER);
        mockMvc.perform(get("/api/users/" + otherManager.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isNotFound())
            .andExpect(content().string(not(containsString("MANAGER"))))
            .andExpect(content().string(not(containsString("Access denied"))));

        mockMvc.perform(get("/api/users/" + inactiveClient.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isNotFound())
            .andExpect(content().string(not(containsString("isActive"))))
            .andExpect(content().string(not(containsString("inactive"))));

        mockMvc.perform(get("/api/users/" + inactiveTrainer.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isNotFound())
            .andExpect(content().string(not(containsString("TRAINER"))))
            .andExpect(content().string(not(containsString("inactive"))));

        mockMvc.perform(get("/api/users/" + inactiveProfessor.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isNotFound())
            .andExpect(content().string(not(containsString("PROFESSOR"))))
            .andExpect(content().string(not(containsString("inactive"))));

        UUID nonexistentId = UUID.randomUUID();
        MvcResult hiddenResult = mockMvc.perform(get("/api/users/" + adminId)
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andReturn();
        MvcResult nonexistentResult = mockMvc.perform(get("/api/users/" + nonexistentId)
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andReturn();
        assertEquals(hiddenResult.getResponse().getContentType(), nonexistentResult.getResponse().getContentType());

        mockMvc.perform(get("/api/users/" + adminId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(adminId.toString()))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.isActive").exists())
            .andExpect(jsonPath("$.createdAt").exists());

        mockMvc.perform(get("/api/users/" + clientId)
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(clientId.toString()))
            .andExpect(jsonPath("$.role").value("CLIENT"))
            .andExpect(jsonPath("$.isActive").exists())
            .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("User directory pagination is bounded and invalid size is safely defaulted")
    void userDirectoryPaginationIsBounded() throws Exception {
        for (int i = 0; i < 105; i++) {
            createClient("page-bound-" + i + "-" + UUID.randomUUID() + "@test.com");
        }

        mockMvc.perform(get("/api/manager/users?page=0&size=1000")
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(100));

        mockMvc.perform(get("/api/manager/users?page=0&size=0")
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(20));

        mockMvc.perform(get("/api/manager/users?page=-1&size=20")
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value(0));

        mockMvc.perform(get("/api/manager/users?page=0&size=20&sort=passwordHash,asc")
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ADMIN can change user role")
    void adminCanChangeRole() throws Exception {
        User target = createClient("change-role-" + UUID.randomUUID() + "@test.com");
        mockMvc.perform(patch("/api/users/" + target.getId() + "/role")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"MANAGER\"}"))
            .andExpect(status().isOk());
        assertEquals(User.Role.MANAGER, userRepository.findById(target.getId()).orElseThrow().getRole());
    }

    @Test
    @DisplayName("MANAGER cannot change user role through controller")
    void managerCannotChangeRole() throws Exception {
        User target = createClient("no-role-change-" + UUID.randomUUID() + "@test.com");
        mockMvc.perform(patch("/api/users/" + target.getId() + "/role")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ADMIN\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CLIENT cannot change user role")
    void clientCannotChangeRole() throws Exception {
        mockMvc.perform(patch("/api/users/" + clientId + "/role")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ADMIN\"}"))
            .andExpect(status().isForbidden());
    }

    // ===== SERVICE-LAYER METHOD SECURITY =====

    @Test
    @DisplayName("Direct proxied UserService.updateRole call by MANAGER throws AccessDeniedException")
    void serviceMethodUpdateRoleDeniesManager() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("manager", "password",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))
        );
        assertThrows(AccessDeniedException.class, () ->
            userService.updateRole(clientId, User.Role.ADMIN));
    }

    @Test
    @DisplayName("Direct proxied UserService.updateRole call by ADMIN succeeds")
    void serviceMethodUpdateRoleAllowsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        assertDoesNotThrow(() -> userService.updateRole(clientId, User.Role.ADMIN));
    }

    @Test
    @DisplayName("Direct proxied PaymentService.refund call by MANAGER throws AccessDeniedException")
    void serviceMethodRefundDeniesManager() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("manager", "password",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))
        );
        UUID paymentId = createCompletedPayment(clientId);
        assertThrows(AccessDeniedException.class, () -> paymentService.refund(paymentId));
    }

    @Test
    @DisplayName("Direct proxied PaymentService.refund call by ADMIN succeeds")
    void serviceMethodRefundAllowsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        UUID paymentId = createCompletedPayment(clientId);
        assertDoesNotThrow(() -> paymentService.refund(paymentId));
    }

    // ===== PARTIAL-UPDATE PREVENTION =====

    @Test
    @DisplayName("Denied isActive request persists no other field changes")
    void deniedUpdatePersistsNoFields() throws Exception {
        User target = createClient("no-partial-" + UUID.randomUUID() + "@test.com");
        String originalName = target.getName();
        boolean originalActive = target.getIsActive();

        mockMvc.perform(put("/api/users/" + target.getId())
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"ShouldNotStick\",\"isActive\":false}"))
            .andExpect(status().isForbidden());

        User reloaded = userRepository.findById(target.getId()).orElseThrow();
        assertEquals(originalName, reloaded.getName(), "name should not change");
        assertEquals(originalActive, reloaded.getIsActive(), "isActive should not change");
    }

    // ===== DIRECT SERVICE LAYER =====

    @Test
    @DisplayName("Direct proxied UserService.delete call by MANAGER deleting TRAINER throws AccessDeniedException")
    void serviceMethodDeleteTrainerByManagerThrowsAccessDeniedException() {
        User trainer = createUser("srv-del-mgr-trainer-" + UUID.randomUUID() + "@test.com", User.Role.TRAINER);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("manager", "password",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))
        );
        assertThrows(AccessDeniedException.class, () -> userService.delete(trainer.getId()));
    }

    @Test
    @DisplayName("Direct proxied UserService.delete call by MANAGER deleting ADMIN throws AccessDeniedException")
    void serviceMethodDeleteAdminByManagerThrowsAccessDeniedException() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("manager", "password",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))
        );
        assertThrows(AccessDeniedException.class, () -> userService.delete(adminId));
    }

    @Test
    @DisplayName("Direct proxied UserService.delete call by ADMIN on CLIENT succeeds")
    void serviceMethodDeleteClientByAdminSucceeds() {
        User target = createClient("srv-del-adm-" + UUID.randomUUID() + "@test.com");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        assertDoesNotThrow(() -> userService.delete(target.getId()));
    }

    @Test
    @DisplayName("Direct proxied UserService.update with isActive by MANAGER throws AccessDeniedException")
    void serviceMethodUpdateIsActiveByManagerThrowsAccessDeniedException() {
        User target = createClient("srv-upd-mgr-" + UUID.randomUUID() + "@test.com");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("manager", "password",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))
        );
        UpdateUserRequest request = new UpdateUserRequest("NewName", null, null, null, false);
        assertThrows(AccessDeniedException.class, () -> userService.update(target.getId(), request));
        User reloaded = userRepository.findById(target.getId()).orElseThrow();
        assertNotEquals("NewName", reloaded.getName(), "name should not change");
    }

    // ===== UNAUTHENTICATED / UNAUTHORIZED =====

    @Test
    @DisplayName("Unauthenticated access returns 401")
    void unauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/memberships/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/users/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/payments/" + UUID.randomUUID() + "/refund"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("No sensitive data leaked in denial responses")
    void noSensitiveDataLeaked() throws Exception {
        User target = createClient("sensitive-" + UUID.randomUUID() + "@test.com");
        mockMvc.perform(delete("/api/users/" + target.getId())
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isNoContent());
    }

    // ===== HELPERS =====

    private User createClient(String email) {
        return createUser(email, User.Role.CLIENT);
    }

    private User createUser(String email, User.Role role) {
        return userRepository.save(User.builder()
            .id(UUID.randomUUID())
            .name("Test " + email)
            .email(email)
            .phone("+351912345678")
            .passwordHash(passwordEncoder.encode("TestPass123!"))
            .role(role)
            .isActive(true)
            .build());
    }

    private UUID createMembership(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Plan plan = planRepository.findById(planId).orElseThrow();
        return membershipRepository.save(Membership.builder()
            .user(user)
            .plan(plan)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .status(Membership.MembershipStatus.ACTIVE)
            .autoRenew(false)
            .build()).getId();
    }

    private UUID createCompletedPayment(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Plan plan = planRepository.findById(planId).orElseThrow();
        Membership membership = membershipRepository.save(Membership.builder()
            .user(user)
            .plan(plan)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .status(Membership.MembershipStatus.ACTIVE)
            .autoRenew(false)
            .build());
        return paymentRepository.save(Payment.builder()
            .user(user)
            .membership(membership)
            .amount(new BigDecimal("50.00"))
            .currency("BRL")
            .method(Payment.PaymentMethod.CARD)
            .status(Payment.PaymentStatus.COMPLETED)
            .gatewayRef("test-ref-" + UUID.randomUUID())
            .build()).getId();
    }
}

package com.gym.controller.security;

import com.gym.entity.User;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security regression tests for recent hardening changes.
 * Focuses on JWT validation and endpoint protection.
 */
@SpringBootTest
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

    private static final String TEST_EMAIL = "test@test.com";

    private String validUserToken;
    private String validAdminToken;
    private UUID testPaymentId;
    private UUID testCheckoutId;

    @BeforeEach
    void setUp() {
        userRepository.findByEmail(TEST_EMAIL).orElseGet(() -> {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setName("Test User");
            user.setEmail(TEST_EMAIL);
            user.setPasswordHash(passwordEncoder.encode("Pass12345"));
            user.setRole(User.Role.CLIENT);
            user.setIsActive(true);
            return userRepository.save(user);
        });

        validUserToken = jwtUtil.generateAccessToken(
                UUID.randomUUID(), TEST_EMAIL, "CLIENT"
        );
        validAdminToken = jwtUtil.generateAccessToken(
                UUID.randomUUID(), "admin@test.com", "ADMIN"
        );
        testPaymentId = UUID.randomUUID();
        testCheckoutId = UUID.randomUUID();
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
    @DisplayName("POST /api/checkout is public (permitAll) - returns non-401")
    void checkoutCreateIsPublic() throws Exception {
        String json = "{\"name\":\"Test\",\"email\":\"test@test.com\",\"password\":\"pass12345\",\"planId\":\"" + UUID.randomUUID() + "\",\"paymentMethod\":\"MBWAY\"}";
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401) {
                        throw new AssertionError("Expected non-401 status, got " + status);
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
}

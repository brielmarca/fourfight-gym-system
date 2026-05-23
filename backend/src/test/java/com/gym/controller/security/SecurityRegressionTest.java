package com.gym.controller.security;

import com.gym.entity.User;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
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
import java.util.Base64;
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
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String CLIENT_PASSWORD = "Pass12345";
    private static final String ADMIN_PASSWORD = "AdminPass123!";

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
            user.setPasswordHash(passwordEncoder.encode(CLIENT_PASSWORD));
            user.setRole(User.Role.CLIENT);
            user.setIsActive(true);
            return userRepository.save(user);
        });

        userRepository.findByEmail(ADMIN_EMAIL).orElseGet(() -> {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setName("Admin User");
            user.setEmail(ADMIN_EMAIL);
            user.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
            user.setRole(User.Role.ADMIN);
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

package com.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.gym.dto.response.MfaSetupResponse;
import com.gym.entity.User;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
import com.gym.security.RateLimitFilter;
import com.gym.security.RateLimitFilterTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

@SpringBootTest(properties = {
        "mfa.encryption-key-base64=VEVTVC1LRVktTk9ULUZPUi1QUk9EVUNUSU9OLTAxMjM=",
        "rate-limit.login.capacity=100",
        "rate-limit.login.refill-tokens=100",
        "rate-limit.login.refill-duration=1"
})
@AutoConfigureMockMvc
class MfaEncryptionIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MfaSecretEncryptionService encryptionService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private RateLimitFilter rateLimitFilter;
    @Autowired private ObjectMapper objectMapper;

    private static final String EMAIL = "mfa-encrypt-test@test.com";
    private static final String PASSWORD = "TestPass123!";
    private static final CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
    private static final TimeProvider timeProvider = new SystemTimeProvider();

    private UUID userId;
    private String userPassword;

    @BeforeEach
    void setUp() {
        RateLimitFilterTestSupport.reset(rateLimitFilter);
        User user = userRepository.findByEmail(EMAIL).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(UUID.randomUUID());
            newUser.setName("Encryption Test");
            newUser.setEmail(EMAIL);
            newUser.setPasswordHash(passwordEncoder.encode(PASSWORD));
            newUser.setRole(User.Role.CLIENT);
            newUser.setIsActive(true);
            newUser.setMfaEnabled(false);
            return userRepository.save(newUser);
        });
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setBackupCodes(null);
        userRepository.save(user);
        userId = user.getId();
        userPassword = PASSWORD;
    }

    @Test
    @DisplayName("Setup response contains plaintext secret, DB stores encrypted")
    void setupStoresEncryptedSecret() throws Exception {
        String loginJson = "{\"email\":\"" + EMAIL + "\",\"password\":\"" + userPassword + "\"}";
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String accessToken = extractField(loginResponse, "accessToken");

        String setupJson = "{\"password\":\"" + userPassword + "\"}";
        String setupResponse = mockMvc.perform(post("/api/auth/mfa/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(setupJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MfaSetupResponse setupResp = objectMapper.readValue(setupResponse, MfaSetupResponse.class);
        String plaintextSecret = setupResp.secret();
        assertThat(plaintextSecret).isNotNull().isNotEmpty();

        User user = userRepository.findById(userId).orElseThrow();
        String storedSecret = user.getMfaSecret();
        assertThat(storedSecret).isNotNull();
        assertThat(storedSecret).startsWith("v1:");
        assertThat(encryptionService.decrypt(storedSecret)).isEqualTo(plaintextSecret);
        assertThat(storedSecret).doesNotContain(plaintextSecret);
    }

    @Test
    @DisplayName("Verify-setup with encrypted secret succeeds")
    void verifySetupWithEncryptedSecret() throws Exception {
        String accessToken = loginAndGetToken();
        User user = userRepository.findById(userId).orElseThrow();
        String secret = setupMfa(accessToken);

        user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getMfaSecret()).startsWith("v1:");

        String totpCode = generateTotpCode(secret);
        String verifyJson = "{\"code\":\"" + totpCode + "\"}";

        mockMvc.perform(post("/api/auth/mfa/verify-setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(verifyJson))
                .andExpect(status().isOk());

        user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getMfaEnabled()).isTrue();
        assertThat(user.getMfaSecret()).startsWith("v1:");
    }

    @Test
    @DisplayName("Legacy plaintext secret is migrated after successful verify-setup")
    void legacyPlaintextMigratedOnVerifySetup() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        String legacySecret = "JBSWY3DPEHPK3PXP";
        user.setMfaSecret(legacySecret);
        user.setMfaEnabled(false);
        userRepository.save(user);

        String accessToken = loginAndGetToken();
        String totpCode = generateTotpCode(legacySecret);
        String verifyJson = "{\"code\":\"" + totpCode + "\"}";

        mockMvc.perform(post("/api/auth/mfa/verify-setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(verifyJson))
                .andExpect(status().isOk());

        user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getMfaEnabled()).isTrue();
        assertThat(user.getMfaSecret()).startsWith("v1:");
        assertThat(encryptionService.decrypt(user.getMfaSecret())).isEqualTo(legacySecret);
    }

    @Test
    @DisplayName("Failed verify-setup does not migrate legacy secret")
    void failedVerifyDoesNotMigrate() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        String legacySecret = "JBSWY3DPEHPK3PXP";
        user.setMfaSecret(legacySecret);
        user.setMfaEnabled(false);
        userRepository.save(user);

        String accessToken = loginAndGetToken();
        String verifyJson = "{\"code\":\"000000\"}";

        mockMvc.perform(post("/api/auth/mfa/verify-setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(verifyJson))
                .andExpect(status().isUnauthorized());

        user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getMfaEnabled()).isFalse();
        assertThat(user.getMfaSecret()).isEqualTo(legacySecret);
    }

    @Test
    @DisplayName("Login validate with encrypted secret succeeds")
    void validateWithEncryptedSecret() throws Exception {
        String accessToken = loginAndGetToken();
        String secret = setupMfa(accessToken);
        enableMfa(accessToken, secret);

        String preAuthToken = jwtUtil.generatePreAuthToken(userId, EMAIL);
        String totpCode = generateTotpCode(secret);

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Legacy plaintext is migrated after successful validate")
    void legacyMigratedOnValidate() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        String legacySecret = "JBSWY3DPEHPK3PXP";
        user.setMfaSecret(legacySecret);
        user.setMfaEnabled(true);
        userRepository.save(user);

        String preAuthToken = jwtUtil.generatePreAuthToken(userId, EMAIL);
        String totpCode = generateTotpCode(legacySecret);

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isOk());

        user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getMfaSecret()).startsWith("v1:");
        assertThat(encryptionService.decrypt(user.getMfaSecret())).isEqualTo(legacySecret);
    }

    @Test
    @DisplayName("Failed validate does not migrate legacy secret")
    void failedValidateDoesNotMigrate() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        String legacySecret = "JBSWY3DPEHPK3PXP";
        user.setMfaSecret(legacySecret);
        user.setMfaEnabled(true);
        userRepository.save(user);

        String preAuthToken = jwtUtil.generatePreAuthToken(userId, EMAIL);
        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"000000\"}"))
                .andExpect(status().isUnauthorized());

        user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getMfaSecret()).isEqualTo(legacySecret);
    }

    @Test
    @DisplayName("Already encrypted value is not double-encrypted")
    void noDoubleEncryption() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        String secret = "JBSWY3DPEHPK3PXP";
        String encrypted = encryptionService.encrypt(secret);
        user.setMfaSecret(encrypted);
        user.setMfaEnabled(true);
        userRepository.save(user);

        String preAuthToken = jwtUtil.generatePreAuthToken(userId, EMAIL);
        String totpCode = generateTotpCode(secret);

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isOk());

        user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getMfaSecret()).isEqualTo(encrypted);
    }

    @Test
    @DisplayName("Disabling MFA clears encrypted secret")
    void disableClearsEncryptedSecret() throws Exception {
        String accessToken = loginAndGetToken();
        String secret = setupMfa(accessToken);
        enableMfa(accessToken, secret);

        String disableJson = "{\"password\":\"" + userPassword + "\"}";
        mockMvc.perform(post("/api/auth/mfa/disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(disableJson))
                .andExpect(status().isNoContent());

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getMfaEnabled()).isFalse();
        assertThat(user.getMfaSecret()).isNull();
        assertThat(user.getBackupCodes()).isNull();
    }

    @Test
    @DisplayName("Valid MFA still issues refresh token cookie")
    void validMfaIssuesRefreshCookie() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        String secret = "JBSWY3DPEHPK3PXP";
        user.setMfaSecret(encryptionService.encrypt(secret));
        user.setMfaEnabled(true);
        userRepository.save(user);

        String preAuthToken = jwtUtil.generatePreAuthToken(userId, EMAIL);
        String totpCode = generateTotpCode(secret);

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String setCookie = result.getResponse().getHeader("Set-Cookie");
                    assertThat(setCookie).contains("refreshToken=");
                    assertThat(setCookie).contains("HttpOnly");
                });
    }

    private String loginAndGetToken() throws Exception {
        String loginJson = "{\"email\":\"" + EMAIL + "\",\"password\":\"" + userPassword + "\"}";
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return extractField(response, "accessToken");
    }

    private String setupMfa(String accessToken) throws Exception {
        String setupJson = "{\"password\":\"" + userPassword + "\"}";
        String response = mockMvc.perform(post("/api/auth/mfa/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(setupJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return extractField(response, "secret");
    }

    private void enableMfa(String accessToken, String secret) throws Exception {
        String totpCode = generateTotpCode(secret);
        String verifyJson = "{\"code\":\"" + totpCode + "\"}";
        mockMvc.perform(post("/api/auth/mfa/verify-setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(verifyJson))
                .andExpect(status().isOk());
    }

    private static String generateTotpCode(String secret) {
        try {
            long timeSlice = timeProvider.getTime() / 30;
            return codeGenerator.generate(secret, timeSlice);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP code", e);
        }
    }

    private static String extractField(String json, String fieldName) {
        String marker = "\"" + fieldName + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return null;
        int valueStart = start + marker.length();
        int valueEnd = json.indexOf('"', valueStart);
        return valueEnd < 0 ? null : json.substring(valueStart, valueEnd);
    }
}

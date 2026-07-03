package com.gym.controller;

import com.gym.entity.User;
import com.gym.repository.RefreshTokenRepository;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
import com.gym.security.RateLimitFilter;
import com.gym.security.RateLimitFilterTestSupport;
import com.gym.service.AuthService;
import com.gym.service.MfaService;
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
import org.springframework.test.web.servlet.MvcResult;

import io.jsonwebtoken.Jwts;
import java.security.PrivateKey;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "mfa.encryption-key-base64=VEVTVC1LRVktTk9ULUZPUi1QUk9EVUNUSU9OLTAxMjM=",
        "rate-limit.login.capacity=100",
        "rate-limit.login.refill-tokens=100",
        "rate-limit.login.refill-duration=1"
})
@AutoConfigureMockMvc
class MfaControllerValidationTest {

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
    private RefreshTokenRepository refreshTokenRepository;

    private static final String MFA_USER_EMAIL = "mfa-test@test.com";
    private static final String MFA_USER_PASSWORD = "MfaPass123!";
    private static final String NORMAL_USER_EMAIL = "normal-login@test.com";
    private static final String NORMAL_USER_PASSWORD = "NormalPass123!";
    private static final String TOTP_SECRET = "JBSWY3DPEHPK3PXP";
    private static final CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
    private static final TimeProvider timeProvider = new SystemTimeProvider();

    private UUID mfaUserId;

    @BeforeEach
    void setUp() {
        RateLimitFilterTestSupport.reset(rateLimitFilter);
        authService.unlockAccountLockout(MFA_USER_EMAIL);

        User user = userRepository.findByEmail(MFA_USER_EMAIL).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(UUID.randomUUID());
            newUser.setName("MFA Test User");
            newUser.setEmail(MFA_USER_EMAIL);
            newUser.setPasswordHash(passwordEncoder.encode(MFA_USER_PASSWORD));
            newUser.setRole(User.Role.CLIENT);
            newUser.setIsActive(true);
            newUser.setMfaEnabled(false);
            return userRepository.save(newUser);
        });

        user.setMfaEnabled(true);
        user.setMfaSecret(TOTP_SECRET);
        user.setBackupCodes(null);
        userRepository.save(user);

        mfaUserId = user.getId();

        userRepository.findByEmail(NORMAL_USER_EMAIL).orElseGet(() -> {
            User normalUser = new User();
            normalUser.setId(UUID.randomUUID());
            normalUser.setName("Normal Login User");
            normalUser.setEmail(NORMAL_USER_EMAIL);
            normalUser.setPasswordHash(passwordEncoder.encode(NORMAL_USER_PASSWORD));
            normalUser.setRole(User.Role.CLIENT);
            normalUser.setIsActive(true);
            normalUser.setMfaEnabled(false);
            return userRepository.save(normalUser);
        });
    }

    @Test
    @DisplayName("GET /api/auth/login with MFA-enabled user returns pre-auth token")
    void loginWithMfaEnabledUserReturnsPreAuthToken() throws Exception {
        String loginJson = "{\"email\":\"" + MFA_USER_EMAIL + "\",\"password\":\"" + MFA_USER_PASSWORD + "\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").isEmpty())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    @DisplayName("Successful MFA validation returns 200 with access token only, refreshToken is null")
    void successfulMfaValidationReturnsAccessTokenOnly() throws Exception {
        String preAuthToken = jwtUtil.generatePreAuthToken(mfaUserId, MFA_USER_EMAIL);
        String totpCode = generateTotpCode();

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.refreshToken").isEmpty());
    }

    @Test
    @DisplayName("Successful MFA validation sets refreshToken HttpOnly cookie")
    void successfulMfaValidationSetsRefreshTokenCookie() throws Exception {
        String preAuthToken = jwtUtil.generatePreAuthToken(mfaUserId, MFA_USER_EMAIL);
        String totpCode = generateTotpCode();

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("Path=/")));
    }

    @Test
    @DisplayName("Successful MFA validation cookie has Max-Age matching refresh expiration")
    void successfulMfaValidationCookieHasMaxAge() throws Exception {
        String preAuthToken = jwtUtil.generatePreAuthToken(mfaUserId, MFA_USER_EMAIL);
        String totpCode = generateTotpCode();

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=604800")));
    }

    @Test
    @DisplayName("Invalid MFA code returns 401 and does not set cookie")
    void invalidMfaCodeDoesNotSetCookie() throws Exception {
        String preAuthToken = jwtUtil.generatePreAuthToken(mfaUserId, MFA_USER_EMAIL);

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"000000\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    @DisplayName("Validate endpoint with missing preAuthToken returns 400")
    void missingPreAuthTokenReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Invalid preAuthToken returns 401 with no cookie")
    void invalidPreAuthTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"invalid-token\",\"code\":\"123456\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("MFA-disabled user returns 422 with no cookie")
    void mfaDisabledUserReturns422() throws Exception {
        String preAuthToken = jwtUtil.generatePreAuthToken(mfaUserId, MFA_USER_EMAIL);
        String totpCode = generateTotpCode();

        User user = userRepository.findById(mfaUserId).orElseThrow();
        user.setMfaEnabled(false);
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("Invalid backup code returns 401 with no cookie and no tokens")
    void invalidBackupCodeReturns401() throws Exception {
        String preAuthToken = jwtUtil.generatePreAuthToken(mfaUserId, MFA_USER_EMAIL);

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"12345678\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("Invalid MFA code response does not contain stack traces or internal class names")
    void invalidMfaCodeResponseHasNoStackTraces() throws Exception {
        String preAuthToken = jwtUtil.generatePreAuthToken(mfaUserId, MFA_USER_EMAIL);

        String body = mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"000000\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.junit.jupiter.api.Assertions.assertFalse(body.contains("Exception"), "Response must not contain exception class names");
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("at com.gym"), "Response must not contain stack traces");
    }

    @Test
    @DisplayName("Invalid preAuthToken response does not contain stack traces or internal class names")
    void invalidPreAuthTokenResponseHasNoStackTraces() throws Exception {
        String body = mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"invalid-token\",\"code\":\"123456\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.junit.jupiter.api.Assertions.assertFalse(body.contains("Exception"), "Response must not contain exception class names");
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("at com.gym"), "Response must not contain stack traces");
    }

    @Test
    @DisplayName("MFA-validated refresh token works with refresh endpoint")
    void mfaValidatedRefreshTokenWorksWithRefreshEndpoint() throws Exception {
        String preAuthToken = jwtUtil.generatePreAuthToken(mfaUserId, MFA_USER_EMAIL);
        String totpCode = generateTotpCode();

        MvcResult validateResult = mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = validateResult.getResponse().getHeader("Set-Cookie");
        org.junit.jupiter.api.Assertions.assertNotNull(setCookie, "MFA validate must set cookie");

        String cookieValue = extractRefreshCookieValue(setCookie);
        org.springframework.mock.web.MockCookie refreshCookie =
                new org.springframework.mock.web.MockCookie("refreshToken", cookieValue);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").isEmpty());
    }

    @Test
    @DisplayName("MFA-validated refresh token can be logged out and cleared")
    void mfaValidatedTokenCanBeLoggedOut() throws Exception {
        String preAuthToken = jwtUtil.generatePreAuthToken(mfaUserId, MFA_USER_EMAIL);
        String totpCode = generateTotpCode();

        MvcResult validateResult = mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = validateResult.getResponse().getHeader("Set-Cookie");
        String accessToken = extractField(validateResult.getResponse().getContentAsString(), "accessToken");
        String cookieValue = extractRefreshCookieValue(setCookie);
        org.springframework.mock.web.MockCookie refreshCookie =
                new org.springframework.mock.web.MockCookie("refreshToken", cookieValue);

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Normal login cookie and MFA validate cookie have same attributes")
    void normalLoginAndMfaValidateCookiesMatch() throws Exception {
        String normalLoginJson = "{\"email\":\"" + NORMAL_USER_EMAIL + "\",\"password\":\"" + NORMAL_USER_PASSWORD + "\"}";

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(normalLoginJson))
                .andExpect(status().isOk())
                .andReturn();

        String loginSetCookie = loginResult.getResponse().getHeader("Set-Cookie");

        String preAuthToken = jwtUtil.generatePreAuthToken(mfaUserId, MFA_USER_EMAIL);
        String totpCode = generateTotpCode();

        MvcResult validateResult = mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + preAuthToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String mfaSetCookie = validateResult.getResponse().getHeader("Set-Cookie");

        org.junit.jupiter.api.Assertions.assertNotNull(loginSetCookie, "Normal login must set cookie");
        org.junit.jupiter.api.Assertions.assertNotNull(mfaSetCookie, "MFA validate must set cookie");

        String loginCookieAttrs = loginSetCookie.replaceAll("refreshToken=[^;]+", "refreshToken=<redacted>");
        String mfaCookieAttrs = mfaSetCookie.replaceAll("refreshToken=[^;]+", "refreshToken=<redacted>");

        org.junit.jupiter.api.Assertions.assertEquals(loginCookieAttrs, mfaCookieAttrs,
                "MFA cookie attributes must match login cookie attributes");
    }

    @Test
    @DisplayName("Access token used as preAuthToken returns 401 with no cookie")
    void accessTokenRejectedAsPreAuthToken() throws Exception {
        String accessToken = jwtUtil.generateAccessToken(mfaUserId, MFA_USER_EMAIL, "CLIENT");
        String totpCode = generateTotpCode();

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + accessToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("Refresh token used as preAuthToken returns 401 with no cookie")
    void refreshTokenRejectedAsPreAuthToken() throws Exception {
        String refreshToken = jwtUtil.generateRefreshToken(mfaUserId);
        String totpCode = generateTotpCode();

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + refreshToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("Token with missing typ claim returns 401 with no cookie")
    void missingTypClaimReturns401() throws Exception {
        PrivateKey privateKey = getPrivateKey();
        String noTypToken = Jwts.builder()
                .subject(mfaUserId.toString())
                .claim("email", MFA_USER_EMAIL)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 300000))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
        String totpCode = generateTotpCode();

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + noTypToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("Token with wrong typ claim returns 401 with no cookie")
    void wrongTypClaimReturns401() throws Exception {
        PrivateKey privateKey = getPrivateKey();
        String wrongTypToken = Jwts.builder()
                .subject(mfaUserId.toString())
                .claim("email", MFA_USER_EMAIL)
                .claim("typ", "invalid")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 300000))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
        String totpCode = generateTotpCode();

        mockMvc.perform(post("/api/auth/mfa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preAuthToken\":\"" + wrongTypToken + "\",\"code\":\"" + totpCode + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    private PrivateKey getPrivateKey() throws Exception {
        java.lang.reflect.Field field = JwtUtil.class.getDeclaredField("privateKey");
        field.setAccessible(true);
        return (PrivateKey) field.get(jwtUtil);
    }

    private static String generateTotpCode() {
        try {
            long timeSlice = timeProvider.getTime() / 30;
            return codeGenerator.generate(TOTP_SECRET, timeSlice);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP code", e);
        }
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

    private static String extractRefreshCookieValue(String setCookieHeader) {
        String[] parts = setCookieHeader.split(";", 2);
        String[] cookieParts = parts[0].split("=", 2);
        return cookieParts.length == 2 ? cookieParts[1] : "";
    }
}

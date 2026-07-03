package com.gym.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(new ClientIpResolver(""));

        ReflectionTestUtils.setField(filter, "loginCapacity", 10);
        ReflectionTestUtils.setField(filter, "registerCapacity", 10);
        ReflectionTestUtils.setField(filter, "loginRefillDurationMinutes", 60);
        ReflectionTestUtils.setField(filter, "refreshCapacity", 10);
        ReflectionTestUtils.setField(filter, "refreshRefillDurationMinutes", 60);
        ReflectionTestUtils.setField(filter, "forgotPasswordCapacity", 2);
        ReflectionTestUtils.setField(filter, "forgotPasswordRefillDurationMinutes", 60);
        ReflectionTestUtils.setField(filter, "resetPasswordCapacity", 2);
        ReflectionTestUtils.setField(filter, "resetPasswordRefillDurationMinutes", 60);
        ReflectionTestUtils.setField(filter, "generalCapacity", 100);
        ReflectionTestUtils.setField(filter, "generalRefillDurationMinutes", 60);

        @SuppressWarnings("unchecked")
        Map<String, ?> buckets = (Map<String, ?>) ReflectionTestUtils.getField(filter, "buckets");
        if (buckets != null) {
            buckets.clear();
        }
    }

    @Test
    @DisplayName("Forgot-password uses dedicated bucket and returns 429 after threshold")
    void forgotPasswordDedicatedBucketRateLimits() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/forgot-password");
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Forwarded-For", "203.0.113.10");
        request.setContentType("application/json");
        request.setContent("{\"email\":\"unknown@test.com\"}".getBytes());

        MockHttpServletResponse response1 = new MockHttpServletResponse();
        filter.doFilter(request, response1, passThroughChain());
        assertEquals(200, response1.getStatus());

        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilter(request, response2, passThroughChain());
        assertEquals(200, response2.getStatus());

        MockHttpServletResponse response3 = new MockHttpServletResponse();
        filter.doFilter(request, response3, passThroughChain());
        assertEquals(429, response3.getStatus());

        String body = response3.getContentAsString();
        assertTrue(body.contains("Too Many Requests"));
        assertFalse(body.contains("unknown@test.com"));
    }

    @Test
    @DisplayName("Reset-password uses dedicated bucket and does not expose secret values")
    void resetPasswordDedicatedBucketRateLimitsAndHidesSecrets() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/reset-password");
        request.setRemoteAddr("203.0.113.11");
        request.addHeader("X-Forwarded-For", "203.0.113.11");
        request.setContentType("application/json");
        request.setContent("{\"token\":\"secret-token\",\"newPassword\":\"SuperSecret123!\"}".getBytes());

        MockHttpServletResponse response1 = new MockHttpServletResponse();
        filter.doFilter(request, response1, passThroughChain());
        assertEquals(200, response1.getStatus());

        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilter(request, response2, passThroughChain());
        assertEquals(200, response2.getStatus());

        MockHttpServletResponse response3 = new MockHttpServletResponse();
        filter.doFilter(request, response3, passThroughChain());
        assertEquals(429, response3.getStatus());

        String body = response3.getContentAsString();
        assertTrue(body.contains("Rate limit exceeded"));
        assertFalse(body.contains("secret-token"));
        assertFalse(body.contains("SuperSecret123!"));
    }

    @Test
    @DisplayName("Endpoint matching keeps dedicated auth routes ahead of general bucket")
    void endpointClassificationCoversDedicatedAndExistingBuckets() {
        assertEquals("forgot-password", ReflectionTestUtils.invokeMethod(filter, "getEndpointType", "/api/auth/forgot-password"));
        assertEquals("reset-password", ReflectionTestUtils.invokeMethod(filter, "getEndpointType", "/api/auth/reset-password"));
        assertEquals("login", ReflectionTestUtils.invokeMethod(filter, "getEndpointType", "/api/auth/login"));
        assertEquals("register", ReflectionTestUtils.invokeMethod(filter, "getEndpointType", "/api/auth/register"));
        assertEquals("refresh", ReflectionTestUtils.invokeMethod(filter, "getEndpointType", "/api/auth/refresh"));
        assertEquals("general", ReflectionTestUtils.invokeMethod(filter, "getEndpointType", "/api/users/me"));
    }

    @Test
    @DisplayName("Direct request with spoofed X-Forwarded-For is ignored")
    void directSpoofedXForwardedForIsIgnored() throws ServletException, IOException {
        filter.doFilter(requestWithRemoteAndHeader("/api/auth/login", "10.0.0.10", "X-Forwarded-For", "203.0.113.20"),
                new MockHttpServletResponse(), passThroughChain());
        filter.doFilter(requestWithRemoteAndHeader("/api/auth/login", "10.0.0.10", "X-Forwarded-For", "203.0.113.21"),
                new MockHttpServletResponse(), passThroughChain());

        assertEquals(1, bucketCount());
        assertTrue(bucketKeys().contains("10.0.0.10:login"));
    }

    @Test
    @DisplayName("Direct request with spoofed X-Real-IP is ignored")
    void directSpoofedXRealIpIsIgnored() throws ServletException, IOException {
        filter.doFilter(requestWithRemoteAndHeader("/api/auth/login", "10.0.0.10", "X-Real-IP", "203.0.113.22"),
                new MockHttpServletResponse(), passThroughChain());

        assertTrue(bucketKeys().contains("10.0.0.10:login"));
        assertFalse(bucketKeys().contains("203.0.113.22:login"));
    }

    @Test
    @DisplayName("Trusted proxy request resolves the legitimate client IP")
    void trustedProxyRequestResolvesClientIp() throws ServletException, IOException {
        filter = new RateLimitFilter(new ClientIpResolver("10.0.0.10"));
        initializeFilterFields();

        filter.doFilter(requestWithRemoteAndHeader("/api/auth/login", "10.0.0.10", "X-Forwarded-For", "203.0.113.23"),
                new MockHttpServletResponse(), passThroughChain());

        assertTrue(bucketKeys().contains("203.0.113.23:login"));
    }

    @Test
    @DisplayName("Trusted proxy with multiple hops uses nearest untrusted address")
    void trustedProxyWithMultipleHopsUsesNearestUntrustedAddress() throws ServletException, IOException {
        filter = new RateLimitFilter(new ClientIpResolver("10.0.0.10, 10.0.0.0/24"));
        initializeFilterFields();

        filter.doFilter(requestWithRemoteAndHeader("/api/auth/login", "10.0.0.10", "X-Forwarded-For", "198.51.100.50, 203.0.113.24, 10.0.0.20"),
                new MockHttpServletResponse(), passThroughChain());

        assertTrue(bucketKeys().contains("203.0.113.24:login"));
        assertFalse(bucketKeys().contains("198.51.100.50:login"));
    }

    @Test
    @DisplayName("Two distinct real clients receive different buckets")
    void distinctRealClientsReceiveDifferentBuckets() throws ServletException, IOException {
        filter.doFilter(requestWithRemote("/api/auth/login", "203.0.113.24"),
                new MockHttpServletResponse(), passThroughChain());
        filter.doFilter(requestWithRemote("/api/auth/login", "203.0.113.25"),
                new MockHttpServletResponse(), passThroughChain());

        assertTrue(bucketKeys().contains("203.0.113.24:login"));
        assertTrue(bucketKeys().contains("203.0.113.25:login"));
        assertEquals(2, bucketCount());
    }

    @Test
    @DisplayName("Same real client reuses the same bucket")
    void sameRealClientReusesBucket() throws ServletException, IOException {
        filter.doFilter(requestWithRemote("/api/auth/login", "203.0.113.25"),
                new MockHttpServletResponse(), passThroughChain());
        filter.doFilter(requestWithRemote("/api/auth/login", "203.0.113.25"),
                new MockHttpServletResponse(), passThroughChain());

        assertEquals(1, bucketCount());
        assertTrue(bucketKeys().contains("203.0.113.25:login"));
    }

    @Test
    @DisplayName("Spoofed headers cannot bypass login rate limit")
    void spoofedHeadersCannotBypassLoginRateLimit() throws ServletException, IOException {
        ReflectionTestUtils.setField(filter, "loginCapacity", 2);

        assertEquals(200, responseFor(requestWithRemoteAndHeader("/api/auth/login", "10.0.0.10", "X-Forwarded-For", "203.0.113.30")).getStatus());
        assertEquals(200, responseFor(requestWithRemoteAndHeader("/api/auth/login", "10.0.0.10", "X-Forwarded-For", "203.0.113.31")).getStatus());
        assertEquals(429, responseFor(requestWithRemoteAndHeader("/api/auth/login", "10.0.0.10", "X-Forwarded-For", "203.0.113.32")).getStatus());
    }

    @Test
    @DisplayName("Spoofed headers cannot bypass general rate limit")
    void spoofedHeadersCannotBypassGeneralRateLimit() throws ServletException, IOException {
        ReflectionTestUtils.setField(filter, "generalCapacity", 2);

        assertEquals(200, responseFor(requestWithRemoteAndHeader("/api/user/me", "10.0.0.10", "X-Forwarded-For", "203.0.113.33")).getStatus());
        assertEquals(200, responseFor(requestWithRemoteAndHeader("/api/user/me", "10.0.0.10", "X-Forwarded-For", "203.0.113.34")).getStatus());
        assertEquals(429, responseFor(requestWithRemoteAndHeader("/api/user/me", "10.0.0.10", "X-Forwarded-For", "203.0.113.35")).getStatus());
    }

    private void initializeFilterFields() {
        ReflectionTestUtils.setField(filter, "loginCapacity", 10);
        ReflectionTestUtils.setField(filter, "registerCapacity", 10);
        ReflectionTestUtils.setField(filter, "loginRefillDurationMinutes", 60);
        ReflectionTestUtils.setField(filter, "refreshCapacity", 10);
        ReflectionTestUtils.setField(filter, "refreshRefillDurationMinutes", 60);
        ReflectionTestUtils.setField(filter, "forgotPasswordCapacity", 2);
        ReflectionTestUtils.setField(filter, "forgotPasswordRefillDurationMinutes", 60);
        ReflectionTestUtils.setField(filter, "resetPasswordCapacity", 2);
        ReflectionTestUtils.setField(filter, "resetPasswordRefillDurationMinutes", 60);
        ReflectionTestUtils.setField(filter, "generalCapacity", 100);
        ReflectionTestUtils.setField(filter, "generalRefillDurationMinutes", 60);
    }

    private MockHttpServletRequest requestWithRemote(String path, String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(remoteAddr);
        return request;
    }

    private MockHttpServletRequest requestWithRemoteAndHeader(String path, String remoteAddr, String headerName, String headerValue) {
        MockHttpServletRequest request = requestWithRemote(path, remoteAddr);
        request.addHeader(headerName, headerValue);
        return request;
    }

    private MockHttpServletResponse responseFor(MockHttpServletRequest request) throws ServletException, IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, passThroughChain());
        return response;
    }

    private int bucketCount() {
        return bucketKeys().size();
    }

    private java.util.Set<String> bucketKeys() {
        @SuppressWarnings("unchecked")
        Map<String, ?> buckets = (Map<String, ?>) ReflectionTestUtils.getField(filter, "buckets");
        return buckets == null ? java.util.Set.of() : buckets.keySet();
    }

    private FilterChain passThroughChain() {
        return (request, response) -> {
        };
    }
}

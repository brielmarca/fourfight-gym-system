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
        filter = new RateLimitFilter();

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

    private FilterChain passThroughChain() {
        return (request, response) -> {
        };
    }
}

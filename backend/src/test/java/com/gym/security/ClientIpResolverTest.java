package com.gym.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientIpResolverTest {

    @Test
    @DisplayName("Direct request with spoofed X-Forwarded-For is ignored")
    void directRequestWithSpoofedXForwardedForIsIgnored() {
        MockHttpServletRequest request = request("203.0.113.10", "198.51.100.10", null);

        assertEquals("203.0.113.10", new ClientIpResolver("").resolve(request));
    }

    @Test
    @DisplayName("Direct request with spoofed X-Real-IP is ignored")
    void directRequestWithSpoofedXRealIpIsIgnored() {
        MockHttpServletRequest request = request("203.0.113.10", null, "198.51.100.10");

        assertEquals("203.0.113.10", new ClientIpResolver("").resolve(request));
    }

    @Test
    @DisplayName("Trusted proxy resolves IPv4 client from X-Forwarded-For")
    void trustedProxyResolvesIpv4Client() {
        MockHttpServletRequest request = request("10.0.0.10", "203.0.113.10", null);

        assertEquals("203.0.113.10", new ClientIpResolver("10.0.0.10").resolve(request));
    }

    @Test
    @DisplayName("Trusted proxy resolves IPv6 client from X-Forwarded-For")
    void trustedProxyResolvesIpv6Client() {
        MockHttpServletRequest request = request("2001:db8:ffff::10", "2001:db8::10", null);

        assertEquals("2001:db8:0:0:0:0:0:10", new ClientIpResolver("2001:db8:ffff::/64").resolve(request));
    }

    @Test
    @DisplayName("Trusted proxy with multiple forwarding hops selects nearest untrusted address")
    void trustedProxyWithMultipleHopsSelectsNearestUntrustedAddress() {
        MockHttpServletRequest request = request("10.0.0.10", "198.51.100.1, 203.0.113.10, 10.0.0.20", null);

        assertEquals("203.0.113.10", new ClientIpResolver("10.0.0.0/24").resolve(request));
    }

    @Test
    @DisplayName("Malformed X-Forwarded-For falls back to immediate remote address")
    void malformedForwardedForFallsBack() {
        MockHttpServletRequest request = request("10.0.0.10", "not an ip", "203.0.113.10");

        assertEquals("10.0.0.10", new ClientIpResolver("10.0.0.10").resolve(request));
    }

    @Test
    @DisplayName("Blank X-Forwarded-For falls back to X-Real-IP from trusted proxy")
    void blankForwardedForFallsBackToRealIp() {
        MockHttpServletRequest request = request("10.0.0.10", " ", "203.0.113.10");

        assertEquals("203.0.113.10", new ClientIpResolver("10.0.0.10").resolve(request));
    }

    @Test
    @DisplayName("Missing forwarding headers fall back to immediate remote address")
    void missingForwardingHeadersFallBack() {
        MockHttpServletRequest request = request("10.0.0.10", null, null);

        assertEquals("10.0.0.10", new ClientIpResolver("10.0.0.10").resolve(request));
    }

    @Test
    @DisplayName("Malformed X-Real-IP falls back to immediate remote address")
    void malformedRealIpFallsBack() {
        MockHttpServletRequest request = request("10.0.0.10", null, "not an ip");

        assertEquals("10.0.0.10", new ClientIpResolver("10.0.0.10").resolve(request));
    }

    @Test
    @DisplayName("Trusted proxy configuration rejects malformed CIDRs")
    void malformedTrustedProxyCidrFailsClearly() {
        assertThrows(IllegalArgumentException.class, () -> new ClientIpResolver("10.0.0.0/99"));
    }

    @Test
    @DisplayName("Production configuration defaults to trust none without explicit trusted proxies")
    void defaultConfigurationTrustsNoProxies() {
        new ApplicationContextRunner()
                .withBean(ClientIpResolver.class)
                .run(context -> {
                    ClientIpResolver resolver = context.getBean(ClientIpResolver.class);
                    MockHttpServletRequest request = request("203.0.113.10", "198.51.100.10", null);

                    assertEquals("203.0.113.10", resolver.resolve(request));
                });
    }

    private MockHttpServletRequest request(String remoteAddr, String xForwardedFor, String xRealIp) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr(remoteAddr);
        if (xForwardedFor != null) {
            request.addHeader("X-Forwarded-For", xForwardedFor);
        }
        if (xRealIp != null) {
            request.addHeader("X-Real-IP", xRealIp);
        }
        return request;
    }
}

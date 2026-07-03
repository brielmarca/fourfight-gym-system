package com.gym.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j for enterprise-grade brute force protection.
 * Implements per-IP rate limiting with configurable thresholds.
 */
@Slf4j
@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ClientIpResolver clientIpResolver;
    
    @Value("${rate-limit.login.capacity:10}")
    private int loginCapacity;

    @Value("${rate-limit.register.capacity:10}")
    private int registerCapacity;
    
    @Value("${rate-limit.login.refill-tokens:5}")
    private int loginRefillTokens;
    
    @Value("${rate-limit.login.refill-duration:1}")
    private int loginRefillDurationMinutes;
    
    @Value("${rate-limit.refresh.capacity:20}")
    private int refreshCapacity;
    
    @Value("${rate-limit.refresh.refill-tokens:10}")
    private int refreshRefillTokens;
    
    @Value("${rate-limit.refresh.refill-duration:1}")
    private int refreshRefillDurationMinutes;

    @Value("${rate-limit.forgot-password.capacity:5}")
    private int forgotPasswordCapacity;

    @Value("${rate-limit.forgot-password.refill-duration:15}")
    private int forgotPasswordRefillDurationMinutes;

    @Value("${rate-limit.reset-password.capacity:10}")
    private int resetPasswordCapacity;

    @Value("${rate-limit.reset-password.refill-duration:15}")
    private int resetPasswordRefillDurationMinutes;

    @Value("${rate-limit.general.capacity:100}")
    private int generalCapacity;
    
    @Value("${rate-limit.general.refill-tokens:60}")
    private int generalRefillTokens;
    
    @Value("${rate-limit.general.refill-duration:1}")
    private int generalRefillDurationMinutes;
    
    public RateLimitFilter(ClientIpResolver clientIpResolver) {
        this.clientIpResolver = clientIpResolver;
        log.info("[STARTUP] START RateLimitFilter constructor");
        log.info("[STARTUP] END RateLimitFilter constructor");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (!(request instanceof HttpServletRequest httpRequest) || 
            !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }
        
        String path = httpRequest.getRequestURI();
        String clientIp = clientIpResolver.resolve(httpRequest);
        
        // Skip rate limiting for public endpoints and health checks
        if (path.equals("/actuator/health") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            chain.doFilter(request, response);
            return;
        }
        
        String endpoint = getEndpointType(path);
        Bucket bucket = buckets.computeIfAbsent(clientIp + ":" + endpoint, 
            k -> createBucket(endpoint));
        
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, endpoint);
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/problem+json");
            httpResponse.getWriter().write(
                "{\"type\":\"https://gym.com/probs/rate-limit\"," +
                "\"title\":\"Too Many Requests\"," +
                "\"status\":429," +
                "\"detail\":\"Rate limit exceeded. Please try again later.\"}"
            );
        }
    }
    
    private String getEndpointType(String path) {
        if (path.equals("/api/auth/login")) return "login";
        if (path.equals("/api/auth/register")) return "register";
        if (path.equals("/api/auth/refresh")) return "refresh";
        if (path.equals("/api/auth/forgot-password")) return "forgot-password";
        if (path.equals("/api/auth/reset-password")) return "reset-password";
        if (path.startsWith("/api/checkout")) return "checkout";
        if (path.startsWith("/api/admin")) return "admin";
        return "general";
    }
    
    private Bucket createBucket(String endpoint) {
        return switch (endpoint) {
            case "login" -> Bucket.builder()
                    .addLimit(Bandwidth.simple(loginCapacity, 
                        Duration.ofMinutes(loginRefillDurationMinutes)))
                    .build();
            case "register" -> Bucket.builder()
                    .addLimit(Bandwidth.simple(registerCapacity,
                        Duration.ofMinutes(loginRefillDurationMinutes)))
                    .build();
            case "refresh" -> Bucket.builder()
                    .addLimit(Bandwidth.simple(refreshCapacity, 
                        Duration.ofMinutes(refreshRefillDurationMinutes)))
                    .build();
            case "forgot-password" -> Bucket.builder()
                    .addLimit(Bandwidth.simple(forgotPasswordCapacity,
                        Duration.ofMinutes(forgotPasswordRefillDurationMinutes)))
                    .build();
            case "reset-password" -> Bucket.builder()
                    .addLimit(Bandwidth.simple(resetPasswordCapacity,
                        Duration.ofMinutes(resetPasswordRefillDurationMinutes)))
                    .build();
            case "checkout" -> Bucket.builder()
                    .addLimit(Bandwidth.simple(20, Duration.ofMinutes(1)))
                    .build();
            case "admin" -> Bucket.builder()
                    .addLimit(Bandwidth.simple(30, Duration.ofMinutes(1)))
                    .build();
            default -> Bucket.builder()
                    .addLimit(Bandwidth.simple(generalCapacity, 
                        Duration.ofMinutes(generalRefillDurationMinutes)))
                    .build();
        };
    }
    
    /**
     * Resets all in-memory rate-limit buckets.
     * Package-private — intended for deterministic test isolation only.
     * Must never be called from runtime application flows.
     */
    void resetBuckets() {
        buckets.clear();
    }

}

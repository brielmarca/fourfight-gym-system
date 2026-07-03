package com.gym.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.lockout")
public record LoginLockoutProperties(
    @DefaultValue("5") int failedAttempts,
    @DefaultValue("PT15M") Duration duration,
    @DefaultValue("10000") int cacheMaxSize,
    @DefaultValue("PT30M") Duration cacheExpiry
) {

    private static final int MAXIMUM_REASONABLE_FAILED_ATTEMPTS = 1_000;
    private static final Duration MAXIMUM_REASONABLE_DURATION = Duration.ofDays(365);
    private static final long MAXIMUM_REASONABLE_CACHE_MAX_SIZE = 10_000_000L;
    private static final Duration MAXIMUM_REASONABLE_CACHE_EXPIRY = Duration.ofDays(30);

    public LoginLockoutProperties {
        if (failedAttempts < 1 || failedAttempts > MAXIMUM_REASONABLE_FAILED_ATTEMPTS) {
            throw new IllegalArgumentException(
                "security.lockout.failed-attempts must be between 1 and " + MAXIMUM_REASONABLE_FAILED_ATTEMPTS);
        }
        if (duration.isNegative() || duration.isZero() || duration.compareTo(MAXIMUM_REASONABLE_DURATION) > 0) {
            throw new IllegalArgumentException(
                "security.lockout.duration must be between PT1S and " + MAXIMUM_REASONABLE_DURATION);
        }
        if (cacheMaxSize < 1 || cacheMaxSize > MAXIMUM_REASONABLE_CACHE_MAX_SIZE) {
            throw new IllegalArgumentException(
                "security.lockout.cache-max-size must be between 1 and " + MAXIMUM_REASONABLE_CACHE_MAX_SIZE);
        }
        if (cacheExpiry.isNegative() || cacheExpiry.isZero() || cacheExpiry.compareTo(MAXIMUM_REASONABLE_CACHE_EXPIRY) > 0) {
            throw new IllegalArgumentException(
                "security.lockout.cache-expiry must be between PT1M and " + MAXIMUM_REASONABLE_CACHE_EXPIRY);
        }
        if (cacheExpiry.compareTo(duration) < 0) {
            throw new IllegalArgumentException(
                "security.lockout.cache-expiry (" + cacheExpiry + ") must be >= "
                    + "security.lockout.duration (" + duration + ")");
        }
    }
}

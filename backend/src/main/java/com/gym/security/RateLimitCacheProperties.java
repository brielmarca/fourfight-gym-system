package com.gym.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rate-limit.cache")
public record RateLimitCacheProperties(
        @DefaultValue("10000") long maximumSize,
        @DefaultValue("30") long expireAfterAccessMinutes
) {

    private static final long MAXIMUM_REASONABLE_CACHE_SIZE = 1_000_000L;
    private static final long MAXIMUM_REASONABLE_EXPIRY_MINUTES = 1_440L;

    public RateLimitCacheProperties {
        if (maximumSize < 1 || maximumSize > MAXIMUM_REASONABLE_CACHE_SIZE) {
            throw new IllegalArgumentException(
                    "rate-limit.cache.maximum-size must be between 1 and " + MAXIMUM_REASONABLE_CACHE_SIZE);
        }
        if (expireAfterAccessMinutes < 1 || expireAfterAccessMinutes > MAXIMUM_REASONABLE_EXPIRY_MINUTES) {
            throw new IllegalArgumentException(
                    "rate-limit.cache.expire-after-access-minutes must be between 1 and "
                            + MAXIMUM_REASONABLE_EXPIRY_MINUTES);
        }
    }
}

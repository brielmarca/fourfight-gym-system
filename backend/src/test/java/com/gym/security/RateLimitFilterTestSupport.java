package com.gym.security;

/**
 * Test-only bridge to RateLimitFilter.resetBuckets(),
 * which is package-private to prevent production callers.
 */
public final class RateLimitFilterTestSupport {

    private RateLimitFilterTestSupport() {
    }

    public static void reset(RateLimitFilter filter) {
        filter.resetBuckets();
    }
}

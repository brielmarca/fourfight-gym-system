package com.gym.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class SubscriptionStatusTest {

    @Test
    void canonicalSpellingIsCANCELLED() {
        Subscription.SubscriptionStatus status =
            assertDoesNotThrow(() -> Subscription.SubscriptionStatus.valueOf("CANCELLED"));
        assertEquals(Subscription.SubscriptionStatus.valueOf("CANCELLED"), status);
    }

    @Test
    void noCanlledConstantExists() {
        boolean hasCanlled = false;
        for (Subscription.SubscriptionStatus s : Subscription.SubscriptionStatus.values()) {
            if (s.name().equals("CANLLED")) {
                hasCanlled = true;
                break;
            }
        }
        assertEquals(false, hasCanlled, "CANLLED must not exist — fix typo in Subscription.java");
    }
}

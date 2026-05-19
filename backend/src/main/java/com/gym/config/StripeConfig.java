package com.gym.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @PostConstruct
    public void init() {
        log.info("[STARTUP] START StripeConfig.init");
        if (secretKey != null && !secretKey.isBlank()) {
            Stripe.apiKey = secretKey;
            log.info("[STARTUP] Stripe API key configured");
        } else {
            log.info("[STARTUP] Stripe API key not configured (skipping)");
        }
        log.info("[STARTUP] END StripeConfig.init");
    }
}

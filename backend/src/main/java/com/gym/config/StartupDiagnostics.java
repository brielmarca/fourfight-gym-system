package com.gym.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupDiagnostics {

    private final long startTime = System.currentTimeMillis();

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() {
        log.info("[STARTUP] Context refreshed in {} ms", elapsed());
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        log.info("[STARTUP] Application started in {} ms", elapsed());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[STARTUP] Application READY in {} ms", elapsed());
    }

    private long elapsed() {
        return System.currentTimeMillis() - startTime;
    }
}

package com.gym.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.cache.caffeine.spec:maximumSize=1000,expireAfterWrite=10m}")
    private String caffeineSpec;

    @Bean
    public CacheManager cacheManager() {
        log.info("[STARTUP] START CacheManager creation");
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.from(caffeineSpec));
        log.info("[STARTUP] END CacheManager creation");
        return cacheManager;
    }
}
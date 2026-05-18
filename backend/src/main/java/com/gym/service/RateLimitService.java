package com.gym.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.gym.entity.RateLimitBucket;
import com.gym.repository.RateLimitBucketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitBucketRepository rateLimitBucketRepository;

    @Value("${rate-limit.global.requests:300}")
    private int globalRequests;

    @Value("${rate-limit.global.window:PT1M}")
    private String globalWindow;

    public boolean tryConsume(String key) {
        return tryConsume(key, 1);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryConsume(String key, int count) {
        RateLimitBucket bucket = rateLimitBucketRepository.findByKey(key)
            .orElseGet(() -> createBucket(key));

        if (bucket.getTokens() < count) {
            log.warn("Rate limit exceeded for key: {}", key);
            return false;
        }

        int consumed = rateLimitBucketRepository.tryConsume(key);
        return consumed > 0;
    }

    private RateLimitBucket createBucket(String key) {
        RateLimitBucket bucket = RateLimitBucket.builder()
            .key(key)
            .tokens(globalRequests)
            .lastRefill(LocalDateTime.now())
            .build();

        return rateLimitBucketRepository.save(bucket);
    }

    public void refill(String key) {
        RateLimitBucket bucket = rateLimitBucketRepository.findByKey(key)
            .orElse(null);
        
        if (bucket != null) {
            bucket.setTokens(globalRequests);
            bucket.setLastRefill(LocalDateTime.now());
            rateLimitBucketRepository.save(bucket);
        }
    }
}
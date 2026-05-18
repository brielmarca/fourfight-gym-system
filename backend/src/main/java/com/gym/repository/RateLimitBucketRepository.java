package com.gym.repository;

import com.gym.entity.RateLimitBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RateLimitBucketRepository extends JpaRepository<RateLimitBucket, UUID> {

    Optional<RateLimitBucket> findByKey(String key);

    @Modifying
    @Query("UPDATE RateLimitBucket rb SET rb.tokens = rb.tokens - 1, rb.updatedAt = CURRENT_TIMESTAMP WHERE rb.key = :key AND rb.tokens > 0")
    int tryConsume(@Param("key") String key);

    boolean existsByKey(String key);
}
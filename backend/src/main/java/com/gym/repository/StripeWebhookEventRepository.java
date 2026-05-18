package com.gym.repository;

import com.gym.entity.StripeWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, UUID> {
    Optional<StripeWebhookEvent> findByEventId(String eventId);
    boolean existsByEventId(String eventId);
}

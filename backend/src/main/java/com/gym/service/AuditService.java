package com.gym.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.gym.entity.AuditLog;
import com.gym.event.EntityAuditEvent;
import com.gym.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityAuditEvent(EntityAuditEvent event) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .action(mapAction(event.getAction()))
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .diffJson(buildDiffJson(event.getBeforeJson(), event.getAfterJson()))
                .ip(event.getIp())
                .userAgent(event.getUserAgent())
                .build();

            if (event.getActorId() != null) {
                auditLog.setActor(null);
            }

            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} {} {}", auditLog.getAction(), auditLog.getEntityType(), auditLog.getEntityId());
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    public void createAuditLog(EntityAuditEvent event) {
        onEntityAuditEvent(event);
    }

    private AuditLog.AuditAction mapAction(EntityAuditEvent.AuditAction action) {
        return switch (action) {
            case CREATE -> AuditLog.AuditAction.CREATE;
            case UPDATE -> AuditLog.AuditAction.UPDATE;
            case DELETE -> AuditLog.AuditAction.DELETE;
        };
    }

    private String buildDiffJson(String before, String after) {
        if (before == null && after == null) return null;
        return String.format("{\"before\": %s, \"after\": %s}", before, after);
    }
}
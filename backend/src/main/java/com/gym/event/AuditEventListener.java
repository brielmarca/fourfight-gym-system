package com.gym.event;

import com.gym.entity.AuditLog;
import com.gym.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityAuditEvent(EntityAuditEvent event) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .action(com.gym.entity.AuditLog.AuditAction.valueOf(event.getAction().name()))
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .diffJson(event.getDiffJson())
                .ip(event.getIp())
                .userAgent(event.getUserAgent())
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created for {} {} on {}", event.getAction(),
                event.getEntityType(), event.getEntityId());
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage());
        }
    }
}
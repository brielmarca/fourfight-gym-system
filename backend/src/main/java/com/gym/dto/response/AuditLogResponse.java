package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.AuditLog;

public record AuditLogResponse(
    UUID id,
    UUID actorId,
    String actorName,
    AuditLog.AuditAction action,
    String entityType,
    UUID entityId,
    String diffJson,
    String ip,
    String userAgent,
    LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
            log.getId(),
            log.getActor() != null ? log.getActor().getId() : null,
            log.getActor() != null ? log.getActor().getName() : null,
            log.getAction(),
            log.getEntityType(),
            log.getEntityId(),
            log.getDiffJson(),
            log.getIp(),
            log.getUserAgent(),
            log.getCreatedAt()
        );
    }
}
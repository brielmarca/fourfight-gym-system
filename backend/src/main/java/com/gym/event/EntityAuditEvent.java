package com.gym.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityAuditEvent {

    private UUID actorId;
    private EntityAuditEvent.AuditAction action;
    private String entityType;
    private UUID entityId;
    private String beforeJson;
    private String afterJson;
    private String ip;
    private String userAgent;

    public String getDiffJson() {
        return afterJson;
    }

    public UUID getActorId() {
        return actorId;
    }

    public EntityAuditEvent.AuditAction toAuditLogAction() {
        return switch (this.action) {
            case CREATE -> EntityAuditEvent.AuditAction.CREATE;
            case UPDATE -> EntityAuditEvent.AuditAction.UPDATE;
            case DELETE -> EntityAuditEvent.AuditAction.DELETE;
        };
    }

    public enum AuditAction {
        CREATE, UPDATE, DELETE
    }
}
package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Notification;

public record NotificationResponse(
    UUID id,
    UUID userId,
    String title,
    String body,
    Notification.NotificationType type,
    Notification.NotificationChannel channel,
    Boolean isRead,
    LocalDateTime readAt,
    LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getUser().getId(),
            notification.getTitle(),
            notification.getBody(),
            notification.getType(),
            notification.getChannel(),
            notification.isRead(),
            notification.getReadAt(),
            notification.getCreatedAt()
        );
    }
}
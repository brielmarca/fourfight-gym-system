package com.gym.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.response.NotificationResponse;
import com.gym.entity.Notification;
import com.gym.entity.Notification.NotificationChannel;
import com.gym.entity.Notification.NotificationType;
import com.gym.entity.User;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Page<NotificationResponse> getAll(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable).map(NotificationResponse::from);
    }

    public Page<NotificationResponse> getUnread(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndReadAtIsNull(userId, pageable).map(NotificationResponse::from);
    }

    public NotificationResponse getById(UUID userId, UUID id) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        return NotificationResponse.from(notification);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID id) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        notification.markAsRead();
        notification = notificationRepository.save(notification);
        return NotificationResponse.from(notification);
    }

    @Transactional
    public NotificationResponse create(User user, String title, String body, Notification.NotificationType type) {
        Notification notification = Notification.builder()
            .user(user)
            .title(title)
            .body(body)
            .type(type)
            .channel(Notification.NotificationChannel.IN_APP)
            .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created for user: {}", user.getEmail());
        return NotificationResponse.from(notification);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        notification.softDelete();
        notificationRepository.save(notification);
        log.info("Notification deleted: {}", id);
    }
}

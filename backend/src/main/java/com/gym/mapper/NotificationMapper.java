package com.gym.mapper;

import com.gym.dto.response.NotificationResponse;
import com.gym.entity.Notification;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    default NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.from(notification);
    }

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}
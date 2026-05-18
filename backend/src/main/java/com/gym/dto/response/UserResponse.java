package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.User;

public record UserResponse(
    UUID id,
    String name,
    String email,
    String phone,
    String avatarUrl,
    User.Role role,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getAvatarUrl(),
            user.getRole(),
            user.getIsActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
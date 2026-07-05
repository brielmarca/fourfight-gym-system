package com.gym.dto.response;

import java.util.UUID;
import com.gym.entity.User;

public record ManagerUserDirectoryResponse(
    UUID id,
    String name,
    String email,
    String phone,
    String avatarUrl,
    User.Role role
) {
    public static ManagerUserDirectoryResponse from(User user) {
        return new ManagerUserDirectoryResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getAvatarUrl(),
            user.getRole()
        );
    }
}

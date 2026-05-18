package com.gym.mapper;

import com.gym.dto.request.RegisterRequest;
import com.gym.dto.request.UpdateUserRequest;
import com.gym.dto.response.UserResponse;
import com.gym.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "CLIENT")
    @Mapping(target = "isActive", constant = "true")
    User toEntity(RegisterRequest request);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateFromRequest(UpdateUserRequest request, @MappingTarget User user);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
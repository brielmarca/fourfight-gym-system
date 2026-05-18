package com.gym.mapper;

import com.gym.dto.request.CreateClassRequest;
import com.gym.dto.response.ClassResponse;
import com.gym.entity.GymClass;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClassMapper {

    @Mapping(target = "trainer", ignore = true)
    @Mapping(target = "status", constant = "SCHEDULED")
    GymClass toEntity(CreateClassRequest request);

    @Mapping(target = "trainerId", source = "trainer.id")
    @Mapping(target = "trainerName", source = "trainer.user.name")
    @Mapping(target = "enrolledCount", ignore = true)
    ClassResponse toResponse(GymClass gymClass);

    List<ClassResponse> toResponseList(List<GymClass> gymClasses);
}
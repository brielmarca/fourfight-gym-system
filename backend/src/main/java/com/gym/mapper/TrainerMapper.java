package com.gym.mapper;

import com.gym.dto.request.CreateTrainerRequest;
import com.gym.dto.response.TrainerResponse;
import com.gym.entity.Trainer;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainerMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "rating", constant = "0")
    Trainer toEntity(CreateTrainerRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userEmail", source = "user.email")
    TrainerResponse toResponse(Trainer trainer);

    List<TrainerResponse> toResponseList(List<Trainer> trainers);
}
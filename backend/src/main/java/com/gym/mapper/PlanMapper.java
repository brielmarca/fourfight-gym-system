package com.gym.mapper;

import com.gym.dto.request.CreatePlanRequest;
import com.gym.dto.response.PlanResponse;
import com.gym.entity.Plan;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlanMapper {

    Plan toEntity(CreatePlanRequest request);

    @Mapping(target = "createdAt", source = "createdAt")
    PlanResponse toResponse(Plan plan);

    List<PlanResponse> toResponseList(List<Plan> plans);
}
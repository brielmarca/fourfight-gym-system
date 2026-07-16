package com.gym.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreatePlanRequest;
import com.gym.dto.request.UpdatePlanRequest;
import com.gym.dto.response.PlanResponse;
import com.gym.entity.Plan;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public Page<PlanResponse> getAll(Pageable pageable) {
        return planRepository.findAll(pageable).map(PlanResponse::from);
    }

    public Page<PlanResponse> getAllActive(Pageable pageable) {
        return planRepository.findByIsActiveTrue(pageable).map(PlanResponse::from);
    }

    public List<PlanResponse> getAllActive() {
        return planRepository.findAll().stream()
                .filter(Plan::getIsActive)
                .map(PlanResponse::from)
                .collect(java.util.stream.Collectors.toList());
    }

    public PlanResponse getById(UUID id) {
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plan", id));
        if (!Boolean.TRUE.equals(plan.getIsActive())) {
            throw new ResourceNotFoundException("Plan", id);
        }
        return PlanResponse.from(plan);
    }

    @Transactional
    public PlanResponse create(CreatePlanRequest request) {
        List<String> features = request.features() != null ? request.features() : new ArrayList<>();
        Plan plan = Plan.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .durationDays(request.durationDays())
            .maxClasses(request.maxClasses())
            .features(features)
            .level(request.level())
            .instructor(request.instructor())
            .schedule(request.schedule())
            .isActive(true)
            .build();

        plan = planRepository.save(plan);
        log.info("Plan created: {}", plan.getName());
        return PlanResponse.from(plan);
    }

    @Transactional
    public PlanResponse update(UUID id, UpdatePlanRequest request) {
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plan", id));

        if (request.name() != null) plan.setName(request.name());
        if (request.description() != null) plan.setDescription(request.description());
        if (request.price() != null) plan.setPrice(request.price());
        if (request.durationDays() != null) plan.setDurationDays(request.durationDays());
        if (request.maxClasses() != null) plan.setMaxClasses(request.maxClasses());
        if (request.features() != null) plan.setFeatures(request.features());
        if (request.level() != null) plan.setLevel(request.level());
        if (request.instructor() != null) plan.setInstructor(request.instructor());
        if (request.schedule() != null) plan.setSchedule(request.schedule());
        if (request.isActive() != null) plan.setIsActive(request.isActive());

        plan = planRepository.save(plan);
        log.info("Plan updated: {}", plan.getName());
        return PlanResponse.from(plan);
    }

    @Transactional
    public void delete(UUID id) {
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plan", id));
        plan.softDelete();
        planRepository.save(plan);
        log.info("Plan deleted: {}", plan.getName());
    }
}

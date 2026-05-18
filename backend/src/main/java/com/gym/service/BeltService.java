package com.gym.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateBeltRequest;
import com.gym.dto.response.BeltResponse;
import com.gym.entity.Belt;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.BeltRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeltService {

    private final BeltRepository beltRepository;

    public List<BeltResponse> getAll() {
        return beltRepository.findAllByOrderByRankOrderAsc().stream()
            .map(BeltResponse::from)
            .toList();
    }

    public List<BeltResponse> getAllActive() {
        return beltRepository.findAllByIsActiveTrueOrderByRankOrderAsc().stream()
            .map(BeltResponse::from)
            .toList();
    }

    public BeltResponse getById(UUID id) {
        Belt belt = beltRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Belt", id));
        return BeltResponse.from(belt);
    }

    @Transactional
    public BeltResponse create(CreateBeltRequest request) {
        Belt belt = Belt.builder()
            .name(request.name())
            .colorHex(request.colorHex())
            .rankOrder(request.rankOrder())
            .isActive(true)
            .build();

        belt = beltRepository.save(belt);
        log.info("Belt created: {}", belt.getName());
        return BeltResponse.from(belt);
    }

    @Transactional
    public BeltResponse update(UUID id, CreateBeltRequest request) {
        Belt belt = beltRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Belt", id));

        if (request.name() != null) belt.setName(request.name());
        if (request.colorHex() != null) belt.setColorHex(request.colorHex());
        if (request.rankOrder() != null) belt.setRankOrder(request.rankOrder());

        belt = beltRepository.save(belt);
        log.info("Belt updated: {}", belt.getName());
        return BeltResponse.from(belt);
    }

    @Transactional
    public void delete(UUID id) {
        Belt belt = beltRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Belt", id));
        belt.softDelete();
        beltRepository.save(belt);
        log.info("Belt deleted: {}", belt.getName());
    }
}
package com.gym.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateGraduationRequest;
import com.gym.dto.request.UpdateGraduationRequest;
import com.gym.dto.response.GraduationResponse;
import com.gym.entity.Graduation;
import com.gym.entity.MartialArt;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.GraduationRepository;
import com.gym.repository.MartialArtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraduationService {

    private final GraduationRepository graduationRepository;
    private final MartialArtRepository martialArtRepository;

    public Page<GraduationResponse> getAll(Pageable pageable) {
        return graduationRepository.findAll(pageable).map(GraduationResponse::from);
    }

    public Page<GraduationResponse> getByMartialArtId(java.util.UUID martialArtId, Pageable pageable) {
        return graduationRepository.findByMartialArtId(martialArtId, pageable).map(GraduationResponse::from);
    }

    public GraduationResponse getById(java.util.UUID id) {
        Graduation graduation = graduationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Graduation", id));
        return GraduationResponse.from(graduation);
    }

    @Transactional
    public GraduationResponse create(CreateGraduationRequest request) {
        MartialArt martialArt = martialArtRepository.findById(request.martialArtId())
            .orElseThrow(() -> new ResourceNotFoundException("MartialArt", request.martialArtId()));
        Graduation graduation = Graduation.builder()
            .name(request.name())
            .levelOrder(request.levelOrder())
            .martialArt(martialArt)
            .build();
        graduation = graduationRepository.save(graduation);
        log.info("Graduation created: {} for martial art: {}", graduation.getName(), martialArt.getName());
        return GraduationResponse.from(graduation);
    }

    @Transactional
    public GraduationResponse update(java.util.UUID id, UpdateGraduationRequest request) {
        Graduation graduation = graduationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Graduation", id));
        if (request.name() != null) graduation.setName(request.name());
        if (request.levelOrder() != null) graduation.setLevelOrder(request.levelOrder());
        if (request.martialArtId() != null) {
            MartialArt martialArt = martialArtRepository.findById(request.martialArtId())
                .orElseThrow(() -> new ResourceNotFoundException("MartialArt", request.martialArtId()));
            graduation.setMartialArt(martialArt);
        }
        graduation = graduationRepository.save(graduation);
        log.info("Graduation updated: {}", graduation.getName());
        return GraduationResponse.from(graduation);
    }

    @Transactional
    public void delete(java.util.UUID id) {
        Graduation graduation = graduationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Graduation", id));
        graduation.softDelete();
        graduationRepository.save(graduation);
        log.info("Graduation deleted: {}", graduation.getName());
    }
}

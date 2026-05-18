package com.gym.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateMartialArtRequest;
import com.gym.dto.request.UpdateMartialArtRequest;
import com.gym.dto.response.MartialArtResponse;
import com.gym.entity.MartialArt;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MartialArtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MartialArtService {

    private final MartialArtRepository martialArtRepository;

    public Page<MartialArtResponse> getAll(Pageable pageable) {
        return martialArtRepository.findAll(pageable).map(MartialArtResponse::from);
    }

    public MartialArtResponse getById(java.util.UUID id) {
        MartialArt martialArt = martialArtRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MartialArt", id));
        return MartialArtResponse.from(martialArt);
    }

    @Transactional
    public MartialArtResponse create(CreateMartialArtRequest request) {
        if (martialArtRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Martial art with name '" + request.name() + "' already exists");
        }
        MartialArt martialArt = MartialArt.builder()
            .name(request.name())
            .build();
        martialArt = martialArtRepository.save(martialArt);
        log.info("Martial art created: {}", martialArt.getName());
        return MartialArtResponse.from(martialArt);
    }

    @Transactional
    public MartialArtResponse update(java.util.UUID id, UpdateMartialArtRequest request) {
        MartialArt martialArt = martialArtRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MartialArt", id));
        if (request.name() != null && !request.name().equals(martialArt.getName())) {
            if (martialArtRepository.existsByName(request.name())) {
                throw new DuplicateResourceException("Martial art with name '" + request.name() + "' already exists");
            }
            martialArt.setName(request.name());
        }
        martialArt = martialArtRepository.save(martialArt);
        log.info("Martial art updated: {}", martialArt.getName());
        return MartialArtResponse.from(martialArt);
    }

    @Transactional
    public void delete(java.util.UUID id) {
        MartialArt martialArt = martialArtRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MartialArt", id));
        martialArt.softDelete();
        martialArtRepository.save(martialArt);
        log.info("Martial art deleted: {}", martialArt.getName());
    }
}

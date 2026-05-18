package com.gym.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateTrainerRequest;
import com.gym.dto.request.UpdateTrainerRequest;
import com.gym.dto.response.TrainerResponse;
import com.gym.entity.User.Role;
import com.gym.entity.Trainer;
import com.gym.entity.User;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.TrainerRepository;
import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;

    public Page<TrainerResponse> getAll(Pageable pageable) {
        return trainerRepository.findAll(pageable).map(TrainerResponse::from);
    }

    public Page<TrainerResponse> getAllActive(Pageable pageable) {
        return trainerRepository.findByIsActiveTrue(pageable).map(TrainerResponse::from);
    }

    public TrainerResponse getById(UUID id) {
        Trainer trainer = trainerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trainer", id));
        return TrainerResponse.from(trainer);
    }

    @Transactional
    public TrainerResponse create(CreateTrainerRequest request) {
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        if (trainerRepository.existsByUserId(request.userId())) {
            throw new DuplicateResourceException("Trainer", "userId", request.userId());
        }

        user.setRole(User.Role.TRAINER);
        userRepository.save(user);

        Trainer trainer = Trainer.builder()
            .user(user)
            .bio(request.bio())
            .specialties(request.specialties())
            .maxClients(request.maxClients() != null ? request.maxClients() : 10)
            .isActive(true)
            .build();

        trainer = trainerRepository.save(trainer);
        log.info("Trainer created for user: {}", user.getEmail());
        return TrainerResponse.from(trainer);
    }

    @Transactional
    public TrainerResponse update(UUID id, UpdateTrainerRequest request) {
        Trainer trainer = trainerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trainer", id));

        if (request.bio() != null) trainer.setBio(request.bio());
        if (request.specialties() != null) trainer.setSpecialties(request.specialties());
        if (request.maxClients() != null) trainer.setMaxClients(request.maxClients());
        if (request.isActive() != null) trainer.setIsActive(request.isActive());

        trainer = trainerRepository.save(trainer);
        log.info("Trainer updated: {}", id);
        return TrainerResponse.from(trainer);
    }

    @Transactional
    public void delete(UUID id) {
        Trainer trainer = trainerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trainer", id));
        trainer.softDelete();
        trainerRepository.save(trainer);
        log.info("Trainer deleted: {}", id);
    }

    public long countActive() {
        return trainerRepository.countActive();
    }
}

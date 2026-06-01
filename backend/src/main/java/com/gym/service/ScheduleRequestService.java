package com.gym.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateScheduleRequestRequest;
import com.gym.dto.response.ScheduleRequestResponse;
import com.gym.entity.ScheduleRequest.RequestStatus;
import com.gym.entity.ScheduleRequest;
import com.gym.entity.Trainer;
import com.gym.entity.User;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.ScheduleRequestRepository;
import com.gym.repository.TrainerRepository;
import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleRequestService {

    private final ScheduleRequestRepository scheduleRequestRepository;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;

    @Transactional(readOnly = true)
    public Page<ScheduleRequestResponse> getAll(Pageable pageable) {
        return scheduleRequestRepository.findAll(pageable).map(ScheduleRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ScheduleRequestResponse> getByUser(UUID userId, Pageable pageable) {
        return scheduleRequestRepository.findByUserId(userId, pageable).map(ScheduleRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ScheduleRequestResponse> getByTrainer(UUID trainerId, Pageable pageable) {
        return scheduleRequestRepository.findByTrainerId(trainerId, pageable).map(ScheduleRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public ScheduleRequestResponse getById(UUID id) {
        ScheduleRequest request = scheduleRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ScheduleRequest", id));
        return ScheduleRequestResponse.from(request);
    }

    @Transactional
    public ScheduleRequestResponse create(UUID userId, CreateScheduleRequestRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Trainer trainer = trainerRepository.findById(request.trainerId())
            .orElseThrow(() -> new ResourceNotFoundException("Trainer", request.trainerId()));

        ScheduleRequest scheduleRequest = ScheduleRequest.builder()
            .user(user)
            .trainer(trainer)
            .preferredAt(request.preferredAt())
            .notes(request.notes())
            .status(ScheduleRequest.RequestStatus.PENDING)
            .build();

        scheduleRequest = scheduleRequestRepository.save(scheduleRequest);
        log.info("Schedule request created for user: {} trainer: {}", userId, request.trainerId());
        return ScheduleRequestResponse.from(scheduleRequest);
    }

    @Transactional
    public ScheduleRequestResponse approve(UUID id) {
        ScheduleRequest request = scheduleRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ScheduleRequest", id));
        request.setStatus(ScheduleRequest.RequestStatus.APPROVED);
        request.resolve();
        request = scheduleRequestRepository.save(request);
        log.info("Schedule request approved: {}", id);
        return ScheduleRequestResponse.from(request);
    }

    @Transactional
    public ScheduleRequestResponse reject(UUID id) {
        ScheduleRequest request = scheduleRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ScheduleRequest", id));
        request.setStatus(ScheduleRequest.RequestStatus.REJECTED);
        request.resolve();
        request = scheduleRequestRepository.save(request);
        log.info("Schedule request rejected: {}", id);
        return ScheduleRequestResponse.from(request);
    }

    @Transactional
    public void delete(UUID id) {
        ScheduleRequest request = scheduleRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ScheduleRequest", id));
        request.softDelete();
        scheduleRequestRepository.save(request);
        log.info("Schedule request deleted: {}", id);
    }
}

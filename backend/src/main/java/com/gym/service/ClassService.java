package com.gym.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateClassRequest;
import com.gym.dto.request.UpdateClassRequest;
import com.gym.dto.response.ClassEnrollmentResponse;
import com.gym.dto.response.ClassResponse;
import com.gym.entity.ClassEnrollment;
import com.gym.entity.GymClass.ClassStatus;
import com.gym.entity.GymClass;
import com.gym.entity.Trainer;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.ClassEnrollmentRepository;
import com.gym.repository.ClassRepository;
import com.gym.repository.TrainerRepository;
import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final TrainerRepository trainerRepository;
    private final ClassEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public Page<ClassResponse> getAll(Pageable pageable) {
        return classRepository.findAll(pageable).map(c -> ClassResponse.from(c, 0));
    }

    public Page<ClassResponse> getUpcoming(Pageable pageable) {
        return classRepository.findUpcoming(pageable).map(c -> {
            long count = enrollmentRepository.countActiveByClassId(c.getId());
            return ClassResponse.from(c, (int) count);
        });
    }

    public ClassResponse getById(UUID id) {
        GymClass gymClass = classRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Class", id));
        long count = enrollmentRepository.countActiveByClassId(id);
        return ClassResponse.from(gymClass, (int) count);
    }

    @Transactional
    public ClassResponse create(CreateClassRequest request) {
        Trainer trainer = trainerRepository.findById(request.trainerId())
            .orElseThrow(() -> new ResourceNotFoundException("Trainer", request.trainerId()));

        GymClass gymClass = GymClass.builder()
            .trainer(trainer)
            .name(request.name())
            .description(request.description())
            .capacity(request.capacity())
            .schedule(request.schedule())
            .durationMin(request.durationMin())
            .isRecurring(request.isRecurring() != null ? request.isRecurring() : false)
            .recurrenceRule(request.recurrenceRule())
            .status(GymClass.ClassStatus.SCHEDULED)
            .build();

        gymClass = classRepository.save(gymClass);
        log.info("Class created: {}", gymClass.getName());
        return ClassResponse.from(gymClass, 0);
    }

    @Transactional
    public ClassResponse update(UUID id, UpdateClassRequest request) {
        GymClass gymClass = classRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Class", id));

        if (request.name() != null) gymClass.setName(request.name());
        if (request.description() != null) gymClass.setDescription(request.description());
        if (request.capacity() != null) gymClass.setCapacity(request.capacity());
        if (request.schedule() != null) gymClass.setSchedule(request.schedule());
        if (request.durationMin() != null) gymClass.setDurationMin(request.durationMin());
        if (request.status() != null) gymClass.setStatus(request.status());
        if (request.isRecurring() != null) gymClass.setIsRecurring(request.isRecurring());
        if (request.recurrenceRule() != null) gymClass.setRecurrenceRule(request.recurrenceRule());

        gymClass = classRepository.save(gymClass);
        log.info("Class updated: {}", gymClass.getName());
        long count = enrollmentRepository.countActiveByClassId(id);
        return ClassResponse.from(gymClass, (int) count);
    }

    @Transactional
    public void delete(UUID id) {
        GymClass gymClass = classRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Class", id));
        gymClass.softDelete();
        classRepository.save(gymClass);
        log.info("Class deleted: {}", id);
    }

    @Transactional
    public ClassEnrollmentResponse enroll(UUID classId, UUID userId) {
        GymClass gymClass = classRepository.findById(classId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", classId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        long currentCount = enrollmentRepository.countActiveByClassId(classId);
        if (currentCount >= gymClass.getCapacity()) {
            throw new BusinessRuleException("CLASS_FULL", "Class is at full capacity");
        }

        if (enrollmentRepository.existsByGymClassIdAndUserId(classId, userId)) {
            throw new DuplicateResourceException("Enrollment", "classId+userId", classId + "+" + userId);
        }

        ClassEnrollment enrollment = ClassEnrollment.builder()
            .gymClass(gymClass)
            .user(user)
            .attended(false)
            .build();

        enrollment = enrollmentRepository.save(enrollment);
        log.info("User {} enrolled in class {}", userId, classId);
        return ClassEnrollmentResponse.from(enrollment);
    }

    @Transactional
    public void unenroll(UUID classId, UUID userId) {
        ClassEnrollment enrollment = enrollmentRepository.findActiveEnrollment(classId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment", classId + "+" + userId));
        enrollment.cancel();
        enrollmentRepository.save(enrollment);
        log.info("User {} unenrolled from class {}", userId, classId);
    }

    public Page<ClassEnrollmentResponse> getRoster(UUID classId, Pageable pageable) {
        return new PageImpl<>(
            enrollmentRepository.findByClassId(classId),
            pageable,
            enrollmentRepository.countActiveByClassId(classId)
        ).map(ClassEnrollmentResponse::from);
    }

    public long getTodayClassCount(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return classRepository.countByScheduleBetween(start, end);
    }
}
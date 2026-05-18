package com.gym.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateStudentProfileRequest;
import com.gym.dto.response.StudentProfileResponse;
import com.gym.entity.Belt;
import com.gym.entity.StudentProfile;
import com.gym.entity.User;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.BeltRepository;
import com.gym.repository.StudentProfileRepository;
import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final BeltRepository beltRepository;

    public StudentProfileResponse getByUserId(UUID userId) {
        StudentProfile profile = studentProfileRepository.findByUserIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", userId));
        return StudentProfileResponse.from(profile);
    }

    public boolean hasProfile(UUID userId) {
        return studentProfileRepository.existsByUserId(userId);
    }

    @Transactional
    public StudentProfileResponse create(CreateStudentProfileRequest request) {
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        if (studentProfileRepository.existsByUserId(request.userId())) {
            throw new DuplicateResourceException("StudentProfile", "userId", request.userId().toString());
        }

        Belt belt = null;
        if (request.beltId() != null) {
            belt = beltRepository.findById(request.beltId()).orElse(null);
        }

        StudentProfile profile = StudentProfile.builder()
            .user(user)
            .belt(belt)
            .trainingDays(request.trainingDays())
            .emergencyContact(request.emergencyContact())
            .emergencyPhone(request.emergencyPhone())
            .medicalNotes(request.medicalNotes())
            .recoveryNotes(request.recoveryNotes())
            .goals(request.goals())
            .observations(request.observations())
            .isActive(true)
            .build();

        profile = studentProfileRepository.save(profile);
        log.info("StudentProfile created for user: {}", user.getEmail());
        return StudentProfileResponse.from(profile);
    }

    @Transactional
    public StudentProfileResponse update(UUID userId, CreateStudentProfileRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", userId));

        if (request.beltId() != null) {
            Belt belt = beltRepository.findById(request.beltId())
                .orElseThrow(() -> new ResourceNotFoundException("Belt", request.beltId()));
            profile.setBelt(belt);
        }
        if (request.trainingDays() != null) profile.setTrainingDays(request.trainingDays());
        if (request.emergencyContact() != null) profile.setEmergencyContact(request.emergencyContact());
        if (request.emergencyPhone() != null) profile.setEmergencyPhone(request.emergencyPhone());
        if (request.medicalNotes() != null) profile.setMedicalNotes(request.medicalNotes());
        if (request.recoveryNotes() != null) profile.setRecoveryNotes(request.recoveryNotes());
        if (request.goals() != null) profile.setGoals(request.goals());
        if (request.observations() != null) profile.setObservations(request.observations());

        profile = studentProfileRepository.save(profile);
        log.info("StudentProfile updated for user: {}", profile.getUser().getEmail());
        return StudentProfileResponse.from(profile);
    }

    @Transactional
    public StudentProfileResponse updateBelt(UUID userId, UUID beltId) {
        StudentProfile profile = studentProfileRepository.findByUserIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", userId));

        Belt belt = beltRepository.findById(beltId)
            .orElseThrow(() -> new ResourceNotFoundException("Belt", beltId));
        profile.setBelt(belt);

        profile = studentProfileRepository.save(profile);
        log.info("Belt updated for user: {} to belt: {}", profile.getUser().getEmail(), belt.getName());
        return StudentProfileResponse.from(profile);
    }
}
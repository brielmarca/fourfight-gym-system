package com.gym.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateStudentRequest;
import com.gym.dto.request.UpdateStudentRequest;
import com.gym.dto.response.StudentResponse;
import com.gym.entity.Plan;
import com.gym.entity.Student;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.PlanRepository;
import com.gym.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final PlanRepository planRepository;

    public Page<StudentResponse> getAll(Pageable pageable) {
        return studentRepository.findAll(pageable).map(StudentResponse::from);
    }

    public StudentResponse getById(UUID id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        return StudentResponse.from(student);
    }

    // Prevent ID enumeration: return same response for unauthorized vs not found
    public StudentResponse getByIdWithOwnership(UUID id, UUID authenticatedUserId, String role) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        
        // CLIENT role can only access their own data
        if ("CLIENT".equals(role)) {
            // In production, you would link the authenticated user to the student entity
            // For now, we throw ResourceNotFoundException to prevent ID enumeration
            throw new ResourceNotFoundException("Student", id);
        }
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Student with email '" + request.email() + "' already exists");
        }
        Student.StudentBuilder builder = Student.builder()
            .name(request.name())
            .email(request.email())
            .isActive(true);
        if (request.planId() != null) {
            Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.planId()));
            builder.plan(plan);
        }
        Student student = builder.build();
        student = studentRepository.save(student);
        log.info("Student created: {} ({})", student.getName(), student.getEmail());
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse update(UUID id, UpdateStudentRequest request, UUID authenticatedUserId, String role) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        
        // CLIENT role can only update their own data
        if ("CLIENT".equals(role)) {
            throw new ResourceNotFoundException("Student", id);
        }
        
        if (request.name() != null) student.setName(request.name());
        if (request.email() != null && !request.email().equals(student.getEmail())) {
            if (studentRepository.existsByEmailAndIdNot(request.email(), id)) {
                throw new DuplicateResourceException("Student with email '" + request.email() + "' already exists");
            }
            student.setEmail(request.email());
        }
        if (request.planId() != null) {
            Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.planId()));
            student.setPlan(plan);
        }
        if (request.isActive() != null) student.setIsActive(request.isActive());
        student = studentRepository.save(student);
        log.info("Student updated: {} ({})", student.getName(), student.getEmail());
        return StudentResponse.from(student);
    }

    @Transactional
    public void delete(UUID id, UUID authenticatedUserId, String role) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        
        // CLIENT role cannot delete
        if ("CLIENT".equals(role)) {
            throw new ResourceNotFoundException("Student", id);
        }
        
        student.softDelete();
        studentRepository.save(student);
        log.info("Student deleted: {} ({})", student.getName(), student.getEmail());
    }
}

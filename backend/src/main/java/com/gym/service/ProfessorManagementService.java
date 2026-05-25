package com.gym.service;

import com.gym.dto.request.AdminCreateProfessorAssignmentRequest;
import com.gym.dto.request.AdminCreateProfessorRequest;
import com.gym.dto.request.AdminUpdateProfessorModalitiesRequest;
import com.gym.dto.response.AdminProfessorAssignmentResponse;
import com.gym.dto.response.AdminProfessorResponse;
import com.gym.dto.response.ProfessorStudentResponse;
import com.gym.entity.Membership;
import com.gym.entity.ProfessorAssignment;
import com.gym.entity.ProfessorModality;
import com.gym.entity.TeachingModality;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.ProfessorAssignmentRepository;
import com.gym.repository.ProfessorModalityRepository;
import com.gym.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfessorManagementService {

    private final UserRepository userRepository;
    private final ProfessorModalityRepository professorModalityRepository;
    private final ProfessorAssignmentRepository professorAssignmentRepository;
    private final MembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public List<AdminProfessorResponse> listProfessors() {
        List<User> professors = userRepository.findAll().stream()
            .filter(u -> u.getRole() == User.Role.PROFESSOR)
            .toList();

        return professors.stream().map(this::toAdminProfessorResponse).toList();
    }

    @Transactional
    public AdminProfessorResponse promoteProfessor(AdminCreateProfessorRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.email()));

        user.setRole(User.Role.PROFESSOR);
        userRepository.save(user);
        replaceModalities(user, request.modalities());
        return toAdminProfessorResponse(user);
    }

    @Transactional
    public AdminProfessorResponse updateProfessorModalities(UUID professorId, AdminUpdateProfessorModalitiesRequest request) {
        User professor = getProfessor(professorId);
        replaceModalities(professor, request.modalities());
        return toAdminProfessorResponse(professor);
    }

    @Transactional(readOnly = true)
    public List<AdminProfessorAssignmentResponse> listAssignments() {
        return professorAssignmentRepository.findAll().stream()
            .sorted(Comparator.comparing(ProfessorAssignment::getAssignedAt).reversed())
            .map(this::toAdminAssignmentResponse)
            .toList();
    }

    @Transactional
    public AdminProfessorAssignmentResponse createAssignment(AdminCreateProfessorAssignmentRequest request, UUID actorId) {
        User professor = getProfessor(request.professorId());
        User student = getActiveStudent(request.studentId());
        validateProfessorCanTeach(professor.getId(), request.modality());

        professorAssignmentRepository.findByProfessorIdAndStudentIdAndModalityAndActiveTrue(
            professor.getId(),
            student.getId(),
            request.modality()
        ).ifPresent(existing -> {
            throw new BusinessRuleException("Active assignment already exists for this professor, student, and modality");
        });

        professorAssignmentRepository.findByStudentIdAndModalityAndActiveTrue(student.getId(), request.modality())
            .ifPresent(existing -> {
                throw new BusinessRuleException("Student already has an active professor for this modality");
            });

        User actor = userRepository.findById(actorId).orElse(null);

        ProfessorAssignment assignment = ProfessorAssignment.builder()
            .professor(professor)
            .student(student)
            .modality(request.modality())
            .active(true)
            .notes(request.notes())
            .assignedBy(actor)
            .build();

        return toAdminAssignmentResponse(professorAssignmentRepository.save(assignment));
    }

    @Transactional
    public AdminProfessorAssignmentResponse deactivateAssignment(UUID assignmentId) {
        ProfessorAssignment assignment = professorAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ResourceNotFoundException("ProfessorAssignment", assignmentId));
        assignment.setActive(false);
        return toAdminAssignmentResponse(professorAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<ProfessorStudentResponse> listProfessorStudents(UUID professorId) {
        getProfessor(professorId);
        return professorAssignmentRepository.findByProfessorIdAndActiveTrueOrderByAssignedAtDesc(professorId).stream()
            .map(assignment -> {
                Membership membership = membershipRepository
                    .findTopByUserIdAndStatusOrderByCreatedAtDesc(assignment.getStudent().getId(), Membership.MembershipStatus.ACTIVE)
                    .orElse(null);
                return new ProfessorStudentResponse(
                    assignment.getStudent().getName(),
                    assignment.getStudent().getEmail(),
                    assignment.getModality(),
                    membership != null ? membership.getStatus() : null,
                    membership != null && membership.getPlan() != null ? membership.getPlan().getName() : null,
                    assignment.getNotes(),
                    assignment.getAssignedAt()
                );
            })
            .toList();
    }

    private User getProfessor(UUID professorId) {
        User professor = userRepository.findById(professorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", professorId));
        if (professor.getRole() != User.Role.PROFESSOR) {
            throw new BusinessRuleException("User is not a professor");
        }
        return professor;
    }

    private User getActiveStudent(UUID studentId) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("User", studentId));
        if (student.getRole() != User.Role.CLIENT || !Boolean.TRUE.equals(student.getIsActive())) {
            throw new BusinessRuleException("Assigned student must be an active student account");
        }
        return student;
    }

    private void replaceModalities(User professor, Set<TeachingModality> modalities) {
        professorModalityRepository.deleteByProfessorId(professor.getId());
        List<ProfessorModality> rows = modalities.stream()
            .map(modality -> ProfessorModality.builder().professor(professor).modality(modality).build())
            .toList();
        professorModalityRepository.saveAll(rows);
    }

    private void validateProfessorCanTeach(UUID professorId, TeachingModality modality) {
        if (!professorModalityRepository.existsByProfessorIdAndModality(professorId, modality)) {
            throw new BusinessRuleException("Professor is not assigned to this modality");
        }
    }

    private AdminProfessorResponse toAdminProfessorResponse(User professor) {
        Set<TeachingModality> modalities = professorModalityRepository.findByProfessorId(professor.getId()).stream()
            .map(ProfessorModality::getModality)
            .collect(Collectors.toSet());
        return new AdminProfessorResponse(professor.getId(), professor.getName(), professor.getEmail(), modalities);
    }

    private AdminProfessorAssignmentResponse toAdminAssignmentResponse(ProfessorAssignment assignment) {
        return new AdminProfessorAssignmentResponse(
            assignment.getId(),
            assignment.getProfessor().getName(),
            assignment.getProfessor().getEmail(),
            assignment.getStudent().getName(),
            assignment.getStudent().getEmail(),
            assignment.getModality(),
            assignment.getActive(),
            assignment.getNotes(),
            assignment.getAssignedAt(),
            assignment.getUpdatedAt()
        );
    }
}

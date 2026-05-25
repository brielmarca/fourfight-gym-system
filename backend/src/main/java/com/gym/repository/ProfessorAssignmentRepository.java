package com.gym.repository;

import com.gym.entity.ProfessorAssignment;
import com.gym.entity.TeachingModality;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorAssignmentRepository extends JpaRepository<ProfessorAssignment, UUID> {
    List<ProfessorAssignment> findByActiveTrueOrderByAssignedAtDesc();
    List<ProfessorAssignment> findByProfessorIdAndActiveTrueOrderByAssignedAtDesc(UUID professorId);
    Optional<ProfessorAssignment> findByProfessorIdAndStudentIdAndModalityAndActiveTrue(
        UUID professorId,
        UUID studentId,
        TeachingModality modality
    );
    Optional<ProfessorAssignment> findByStudentIdAndModalityAndActiveTrue(UUID studentId, TeachingModality modality);
}

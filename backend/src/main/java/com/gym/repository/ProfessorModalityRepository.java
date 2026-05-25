package com.gym.repository;

import com.gym.entity.ProfessorModality;
import com.gym.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorModalityRepository extends JpaRepository<ProfessorModality, UUID> {
    List<ProfessorModality> findByProfessorId(UUID professorId);
    List<ProfessorModality> findByProfessorIn(List<User> professors);
    void deleteByProfessorId(UUID professorId);
    boolean existsByProfessorIdAndModality(UUID professorId, com.gym.entity.TeachingModality modality);
}

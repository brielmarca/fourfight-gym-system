package com.gym.repository;

import com.gym.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    Page<Student> findAll(Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    long count();
    long countByIsActiveTrue();
    long countByPlanIdAndIsActiveTrue(UUID planId);

    Optional<Student> findByEmail(String email);
    Optional<Student> findByEmailIgnoreCase(String email);
}

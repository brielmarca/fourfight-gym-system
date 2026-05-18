package com.gym.repository;

import com.gym.entity.StudentMartialArt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface StudentMartialArtRepository extends JpaRepository<StudentMartialArt, UUID> {
    java.util.List<StudentMartialArt> findByStudentId(UUID studentId);
    boolean existsByStudentIdAndMartialArtId(UUID studentId, UUID martialArtId);
    java.util.Optional<StudentMartialArt> findByIdAndStudentId(UUID id, UUID studentId);
    java.util.List<StudentMartialArt> findTop10ByOrderByCreatedAtDesc();
}

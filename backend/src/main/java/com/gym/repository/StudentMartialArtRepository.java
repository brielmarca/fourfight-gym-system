package com.gym.repository;

import com.gym.entity.StudentMartialArt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface StudentMartialArtRepository extends JpaRepository<StudentMartialArt, UUID> {
    java.util.List<StudentMartialArt> findByStudentId(UUID studentId);
    boolean existsByStudentIdAndMartialArtId(UUID studentId, UUID martialArtId);
    java.util.Optional<StudentMartialArt> findByIdAndStudentId(UUID id, UUID studentId);
    java.util.List<StudentMartialArt> findTop10ByOrderByCreatedAtDesc();

    @Query("""
        SELECT sma
        FROM StudentMartialArt sma
        JOIN FETCH sma.student s
        JOIN FETCH sma.martialArt ma
        JOIN FETCH sma.graduation g
        WHERE LOWER(s.email) IN :emails
        """)
    java.util.List<StudentMartialArt> findAllByStudentEmailIn(@Param("emails") java.util.Collection<String> emails);

    java.util.Optional<StudentMartialArt> findByStudentIdAndMartialArtId(UUID studentId, UUID martialArtId);
}

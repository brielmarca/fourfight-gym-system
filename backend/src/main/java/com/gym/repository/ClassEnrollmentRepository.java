package com.gym.repository;

import com.gym.entity.ClassEnrollment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, UUID> {

    @Query("SELECT ce FROM ClassEnrollment ce WHERE ce.gymClass.id = :classId AND ce.user.id = :userId AND ce.cancelledAt IS NULL")
    Optional<ClassEnrollment> findActiveEnrollment(@Param("classId") UUID classId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(ce) FROM ClassEnrollment ce WHERE ce.gymClass.id = :classId AND ce.cancelledAt IS NULL")
    long countActiveByClassId(@Param("classId") UUID classId);

    @Query("SELECT ce FROM ClassEnrollment ce WHERE ce.gymClass.id = :classId AND ce.cancelledAt IS NULL")
    List<ClassEnrollment> findByClassId(@Param("classId") UUID classId);

    boolean existsByGymClassIdAndUserId(UUID classId, UUID userId);
}
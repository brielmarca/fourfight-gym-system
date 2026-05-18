package com.gym.repository;

import com.gym.entity.GymClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClassRepository extends JpaRepository<GymClass, UUID> {

    Page<GymClass> findByTrainerId(UUID trainerId, Pageable pageable);

    Page<GymClass> findByStatus(GymClass.ClassStatus status, Pageable pageable);

    @Query("SELECT c FROM GymClass c WHERE c.schedule >= :start AND c.schedule < :end")
    List<GymClass> findByScheduleBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c FROM GymClass c WHERE c.schedule >= CURRENT_TIMESTAMP AND c.status = 'SCHEDULED'")
    Page<GymClass> findUpcoming(Pageable pageable);

    @Query("SELECT COUNT(c) FROM GymClass c WHERE c.schedule >= :start AND c.schedule < :end")
    long countByScheduleBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
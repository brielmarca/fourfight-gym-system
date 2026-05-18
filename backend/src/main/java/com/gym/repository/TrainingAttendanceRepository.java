package com.gym.repository;

import com.gym.entity.TrainingAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TrainingAttendanceRepository extends JpaRepository<TrainingAttendance, UUID> {
    List<TrainingAttendance> findByStudentIdAndDateBetween(UUID studentId, LocalDate startDate, LocalDate endDate);
    List<TrainingAttendance> findByStudentIdAndDateBetweenAndPresentTrue(UUID studentId, LocalDate startDate, LocalDate endDate);
    long countByStudentIdAndDateBetweenAndPresentTrue(UUID studentId, LocalDate startDate, LocalDate endDate);
    boolean existsByStudentIdAndDate(UUID studentId, LocalDate date);
    List<TrainingAttendance> findByDate(LocalDate date);
}
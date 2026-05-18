package com.gym.repository;

import com.gym.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(UUID studentId);
    List<Attendance> findByAttendanceDateOrderByCreatedAtDesc(LocalDate date);
    boolean existsByStudentIdAndAttendanceDate(UUID studentId, LocalDate date);
    long countByAttendanceDate(LocalDate date);
}

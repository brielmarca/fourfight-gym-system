package com.gym.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.MarkAttendanceRequest;
import com.gym.dto.response.AttendanceResponse;
import com.gym.entity.Attendance;
import com.gym.entity.Student;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.AttendanceRepository;
import com.gym.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public List<AttendanceResponse> getByStudentId(UUID studentId) {
        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId).stream()
                .map(AttendanceResponse::from)
                .toList();
    }

    public List<AttendanceResponse> getByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDateOrderByCreatedAtDesc(date).stream()
                .map(AttendanceResponse::from)
                .toList();
    }

    public List<AttendanceResponse> getByStudent(UUID studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId).stream()
                .map(AttendanceResponse::from)
                .toList();
    }

    public Long getMonthlyAttendance(UUID studentId) {
        return (long) attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId).size();
    }

    @Transactional
    public AttendanceResponse markAttendance(MarkAttendanceRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.studentId()));

        LocalDate date = request.attendanceDate() != null ? request.attendanceDate() : LocalDate.now();

        if (attendanceRepository.existsByStudentIdAndAttendanceDate(student.getId(), date)) {
            throw new com.gym.exception.BusinessRuleException(
                    "Attendance already marked for student '" + student.getName() + "' on " + date);
        }

        Attendance attendance = Attendance.builder()
                .student(student)
                .attendanceDate(date)
                .build();
        attendance = attendanceRepository.save(attendance);
        log.info("Attendance marked: student={}, date={}", student.getName(), date);
        return AttendanceResponse.from(attendance);
    }

    @Transactional
    public AttendanceResponse create(MarkAttendanceRequest request, UUID userId) {
        return markAttendance(request);
    }

    @Transactional
    public AttendanceResponse update(UUID id, MarkAttendanceRequest request) {
        return null;
    }
}

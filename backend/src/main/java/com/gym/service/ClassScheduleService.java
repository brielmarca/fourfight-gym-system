package com.gym.service;

import com.gym.dto.request.CreateClassScheduleRequest;
import com.gym.dto.request.UpdateClassScheduleRequest;
import com.gym.dto.response.ClassScheduleResponse;
import com.gym.entity.ClassSchedule;
import com.gym.entity.Membership;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.ClassScheduleRepository;
import com.gym.repository.MembershipRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClassScheduleService {

    private final ClassScheduleRepository classScheduleRepository;
    private final MembershipRepository membershipRepository;

    public List<ClassScheduleResponse> getActiveForMember(UUID userId) {
        boolean hasActiveMembership = membershipRepository
            .findByUserIdAndStatus(userId, Membership.MembershipStatus.ACTIVE)
            .stream()
            .anyMatch(Membership::isActive);

        if (!hasActiveMembership) {
            throw new BusinessRuleException(
                "ACTIVE_MEMBERSHIP_REQUIRED",
                "Horários completos disponíveis para membros com plano ativo."
            );
        }

        return classScheduleRepository.findByActiveTrueOrderByDayOfWeekAscStartTimeAsc()
            .stream()
            .sorted(scheduleComparator())
            .map(ClassScheduleResponse::from)
            .toList();
    }

    public List<ClassScheduleResponse> getAllForAdmin() {
        return classScheduleRepository.findAllByOrderByDayOfWeekAscStartTimeAsc()
            .stream()
            .sorted(scheduleComparator())
            .map(ClassScheduleResponse::from)
            .toList();
    }

    @Transactional
    public ClassScheduleResponse create(CreateClassScheduleRequest request) {
        validateTimeRange(request.startTime(), request.endTime());

        ClassSchedule schedule = ClassSchedule.builder()
            .title(request.title().trim())
            .modality(request.modality())
            .dayOfWeek(request.dayOfWeek())
            .startTime(request.startTime())
            .endTime(request.endTime())
            .instructorName(request.instructorName().trim())
            .level(request.level())
            .location(trimToNull(request.location()))
            .capacity(request.capacity())
            .active(request.active() != null ? request.active() : true)
            .notes(trimToNull(request.notes()))
            .build();

        return ClassScheduleResponse.from(classScheduleRepository.save(schedule));
    }

    @Transactional
    public ClassScheduleResponse update(UUID id, UpdateClassScheduleRequest request) {
        ClassSchedule schedule = classScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule", id));

        LocalTimeRange localTimeRange = resolveRange(schedule, request);
        validateTimeRange(localTimeRange.startTime(), localTimeRange.endTime());

        if (request.title() != null) schedule.setTitle(request.title().trim());
        if (request.modality() != null) schedule.setModality(request.modality());
        if (request.dayOfWeek() != null) schedule.setDayOfWeek(request.dayOfWeek());
        if (request.startTime() != null) schedule.setStartTime(request.startTime());
        if (request.endTime() != null) schedule.setEndTime(request.endTime());
        if (request.instructorName() != null) schedule.setInstructorName(request.instructorName().trim());
        if (request.level() != null) schedule.setLevel(request.level());
        if (request.location() != null) schedule.setLocation(trimToNull(request.location()));
        if (request.capacity() != null) schedule.setCapacity(request.capacity());
        if (request.active() != null) schedule.setActive(request.active());
        if (request.notes() != null) schedule.setNotes(trimToNull(request.notes()));

        return ClassScheduleResponse.from(classScheduleRepository.save(schedule));
    }

    @Transactional
    public ClassScheduleResponse deactivate(UUID id) {
        ClassSchedule schedule = classScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule", id));
        schedule.setActive(false);
        return ClassScheduleResponse.from(classScheduleRepository.save(schedule));
    }

    private void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BusinessRuleException(
                "INVALID_SCHEDULE_TIME_RANGE",
                "Start time must be before end time"
            );
        }
    }

    private LocalTimeRange resolveRange(ClassSchedule schedule, UpdateClassScheduleRequest request) {
        return new LocalTimeRange(
            request.startTime() != null ? request.startTime() : schedule.getStartTime(),
            request.endTime() != null ? request.endTime() : schedule.getEndTime()
        );
    }

    private Comparator<ClassSchedule> scheduleComparator() {
        return Comparator
            .comparingInt((ClassSchedule item) -> item.getDayOfWeek().ordinal())
            .thenComparing(ClassSchedule::getStartTime)
            .thenComparing(ClassSchedule::getTitle, String.CASE_INSENSITIVE_ORDER);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record LocalTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {}
}

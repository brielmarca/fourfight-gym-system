package com.gym.repository;

import com.gym.entity.ClassSchedule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, UUID> {

    List<ClassSchedule> findByActiveTrueOrderByDayOfWeekAscStartTimeAsc();

    List<ClassSchedule> findAllByOrderByDayOfWeekAscStartTimeAsc();
}

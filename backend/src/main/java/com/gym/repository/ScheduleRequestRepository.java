package com.gym.repository;

import com.gym.entity.ScheduleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ScheduleRequestRepository extends JpaRepository<ScheduleRequest, UUID> {

    Page<ScheduleRequest> findByUserId(UUID userId, Pageable pageable);

    Page<ScheduleRequest> findByTrainerId(UUID trainerId, Pageable pageable);

    Page<ScheduleRequest> findByStatus(ScheduleRequest.RequestStatus status, Pageable pageable);
}
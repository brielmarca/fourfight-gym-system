package com.gym.repository;

import com.gym.entity.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    Page<Plan> findByIsActiveTrue(Pageable pageable);

    boolean existsByName(String name);

    java.util.List<Plan> findAll();
}
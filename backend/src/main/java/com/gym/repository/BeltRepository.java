package com.gym.repository;

import com.gym.entity.Belt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BeltRepository extends JpaRepository<Belt, UUID> {
    List<Belt> findAllByOrderByRankOrderAsc();
    List<Belt> findAllByIsActiveTrueOrderByRankOrderAsc();
}
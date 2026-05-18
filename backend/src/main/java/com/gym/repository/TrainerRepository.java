package com.gym.repository;

import com.gym.entity.Trainer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, UUID> {

    Optional<Trainer> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    Page<Trainer> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT COUNT(t) FROM Trainer t WHERE t.isActive = true")
    long countActive();
}
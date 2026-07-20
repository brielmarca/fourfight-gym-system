package com.gym.repository;

import com.gym.entity.PreRegistrationProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreRegistrationProfileRepository extends JpaRepository<PreRegistrationProfile, UUID> {

    @EntityGraph(attributePaths = {"user"})
    Page<PreRegistrationProfile> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "preferredTrainingDays"})
    Optional<PreRegistrationProfile> findById(UUID id);

    @EntityGraph(attributePaths = {"user", "preferredTrainingDays"})
    Optional<PreRegistrationProfile> findByUserId(UUID userId);
}

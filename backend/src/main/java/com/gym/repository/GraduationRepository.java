package com.gym.repository;

import com.gym.entity.Graduation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface GraduationRepository extends JpaRepository<Graduation, UUID> {
    Page<Graduation> findByMartialArtId(UUID martialArtId, Pageable pageable);
    Page<Graduation> findAll(Pageable pageable);
}

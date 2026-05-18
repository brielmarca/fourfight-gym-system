package com.gym.repository;

import com.gym.entity.MartialArt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MartialArtRepository extends JpaRepository<MartialArt, UUID> {
    Page<MartialArt> findAll(Pageable pageable);
    boolean existsByName(String name);
    java.util.Optional<MartialArt> findByName(String name);
}

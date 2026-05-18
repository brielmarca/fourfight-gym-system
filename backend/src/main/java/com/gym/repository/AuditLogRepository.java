package com.gym.repository;

import com.gym.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("SELECT al FROM AuditLog al ORDER BY al.createdAt DESC")
    Page<AuditLog> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.actor.id = :actorId OR al.entityType = :entityType")
    Page<AuditLog> findByActorOrEntity(@Param("actorId") UUID actorId, @Param("entityType") String entityType, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.createdAt >= :since")
    Page<AuditLog> findByCreatedAtSince(@Param("since") LocalDateTime since, Pageable pageable);
}
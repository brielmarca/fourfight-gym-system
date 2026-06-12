package com.gym.repository;

import com.gym.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash AND rt.revoked = false AND rt.expiresAt > CURRENT_TIMESTAMP")
    Optional<RefreshToken> findValidByTokenHash(@Param("tokenHash") String tokenHash);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiresAt > CURRENT_TIMESTAMP")
    List<RefreshToken> findAllValidByUserId(@Param("userId") UUID userId);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = true")
    Optional<RefreshToken> findRevokedByUserId(@Param("userId") UUID userId);
}

package com.gym.repository;

import com.gym.entity.Membership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    Page<Membership> findByUserId(UUID userId, Pageable pageable);

    Page<Membership> findByPlanId(UUID planId, Pageable pageable);

    List<Membership> findByUserIdAndStatus(UUID userId, Membership.MembershipStatus status);

    @Query("SELECT m FROM Membership m WHERE m.status = 'ACTIVE' AND m.endDate <= :endDate AND m.autoRenew = false")
    List<Membership> findExpiringSoon(@Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(m) FROM Membership m WHERE m.status = 'ACTIVE' AND m.endDate >= CURRENT_DATE")
    long countActive();

    @Query("SELECT SUM(m.plan.price) FROM Membership m WHERE m.status = 'ACTIVE'")
    java.math.BigDecimal sumActiveMembershipValue();

    Optional<Membership> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    Optional<Membership> findByStripeSubscriptionId(String stripeSubscriptionId);
}
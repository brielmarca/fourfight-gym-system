package com.gym.repository;

import com.gym.entity.PreRegistrationLead;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreRegistrationLeadRepository extends JpaRepository<PreRegistrationLead, UUID> {

    Page<PreRegistrationLead> findAllByOrderBySubmittedAtDesc(Pageable pageable);

    boolean existsByFullNameAndPhoneAndSubmittedAt(String fullName, String phone, LocalDateTime submittedAt);
}

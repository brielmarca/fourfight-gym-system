package com.gym.repository;

import com.gym.entity.PreRegistrationLead;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreRegistrationLeadRepository extends JpaRepository<PreRegistrationLead, UUID> {

    Page<PreRegistrationLead> findAllByOrderBySubmittedAtDesc(Pageable pageable);

    Page<PreRegistrationLead> findAllByStatusNotOrderBySubmittedAtDescIdDesc(String status, Pageable pageable);

    List<PreRegistrationLead> findAllByStatusNotAndPhoneIsNotNull(String status);

    boolean existsByFullNameAndPhoneAndSubmittedAt(String fullName, String phone, LocalDateTime submittedAt);
}

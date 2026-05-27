package com.gym.service;

import com.gym.dto.response.AdminPreRegistrationLeadDetailResponse;
import com.gym.entity.PreRegistrationLead;
import com.gym.repository.AuditLogRepository;
import com.gym.repository.PreRegistrationLeadRepository;
import com.gym.repository.PreRegistrationProfileRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServicePreRegistrationLeadStatusTest {

    @Mock private MembershipService membershipService;
    @Mock private UserService userService;
    @Mock private TrainerService trainerService;
    @Mock private ClassService classService;
    @Mock private PaymentService paymentService;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private PreRegistrationProfileRepository preRegistrationProfileRepository;
    @Mock private PreRegistrationLeadRepository preRegistrationLeadRepository;

    @Test
    void acceptPreRegistration_updatesStatusWithoutMembershipOrPaymentSideEffects() {
        AdminService adminService = new AdminService(
            membershipService,
            userService,
            trainerService,
            classService,
            paymentService,
            auditLogRepository,
            preRegistrationProfileRepository,
            preRegistrationLeadRepository
        );

        UUID leadId = UUID.randomUUID();
        PreRegistrationLead lead = PreRegistrationLead.builder()
            .id(leadId)
            .submittedAt(LocalDateTime.now().minusDays(1))
            .fullName("Lead Test")
            .phone("910000000")
            .source("GOOGLE_FORMS_IMPORT")
            .status("PRE_REGISTERED")
            .build();

        when(preRegistrationLeadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(preRegistrationLeadRepository.save(any(PreRegistrationLead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminPreRegistrationLeadDetailResponse response = adminService.acceptPreRegistration(leadId);

        assertThat(response.status()).isEqualTo("ACCEPTED");
        verify(preRegistrationLeadRepository).save(lead);
        verifyNoInteractions(membershipService, paymentService);
    }

    @Test
    void archivePreRegistration_updatesStatusAndDoesNotDelete() {
        AdminService adminService = new AdminService(
            membershipService,
            userService,
            trainerService,
            classService,
            paymentService,
            auditLogRepository,
            preRegistrationProfileRepository,
            preRegistrationLeadRepository
        );

        UUID leadId = UUID.randomUUID();
        PreRegistrationLead lead = PreRegistrationLead.builder()
            .id(leadId)
            .submittedAt(LocalDateTime.now().minusDays(2))
            .fullName("Archive Test")
            .phone("920000000")
            .source("GOOGLE_FORMS_IMPORT")
            .status("PRE_REGISTERED")
            .build();

        when(preRegistrationLeadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(preRegistrationLeadRepository.save(any(PreRegistrationLead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminPreRegistrationLeadDetailResponse response = adminService.archivePreRegistration(leadId);

        assertThat(response.status()).isEqualTo("ARCHIVED");
        verify(preRegistrationLeadRepository).save(lead);
        verify(preRegistrationLeadRepository, never()).delete(any(PreRegistrationLead.class));
    }
}

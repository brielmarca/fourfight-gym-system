package com.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.dto.response.AdminRegistrationResponse;
import com.gym.entity.PreRegistrationLead;
import com.gym.entity.PreRegistrationProfile;
import com.gym.entity.User;
import com.gym.repository.AuditLogRepository;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PreRegistrationLeadRepository;
import com.gym.repository.PreRegistrationProfileRepository;
import com.gym.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AdminServiceRegistrationTest {

    @Mock private MembershipService membershipService;
    @Mock private UserService userService;
    @Mock private TrainerService trainerService;
    @Mock private ClassService classService;
    @Mock private PaymentService paymentService;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private PreRegistrationProfileRepository preRegistrationProfileRepository;
    @Mock private PreRegistrationLeadRepository preRegistrationLeadRepository;
    @Mock private UserRepository userRepository;
    @Mock private MembershipRepository membershipRepository;
    @Mock private AuthService authService;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(
            membershipService,
            userService,
            trainerService,
            classService,
            paymentService,
            auditLogRepository,
            preRegistrationProfileRepository,
            preRegistrationLeadRepository,
            userRepository,
            membershipRepository,
            authService,
            new ObjectMapper()
        );
    }

    @Test
    void getRegistrations_mergesCsvAndSiteWithFullDetailsAndOrigins() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 12, 0);
        PreRegistrationLead lead = PreRegistrationLead.builder()
            .id(UUID.randomUUID())
            .submittedAt(now.minusHours(1))
            .fullName("CSV Client")
            .age(29)
            .phone("910000001")
            .parish("Centro")
            .trainingGoal("Condicionamento")
            .preferredModalities("Boxe")
            .preferredTrainingTimes("Noite")
            .preferredTrainingDays("Segunda")
            .preferredContactMethod("Mensagem")
            .source("GOOGLE_FORMS_IMPORT")
            .status("PRE_REGISTERED")
            .createdAt(now.minusHours(1))
            .updatedAt(now.minusHours(1))
            .build();
        User user = User.builder()
            .id(UUID.randomUUID())
            .name("Site Client")
            .email("site@test.com")
            .role(User.Role.CLIENT)
            .isActive(true)
            .createdAt(now)
            .updatedAt(now)
            .build();
        PreRegistrationProfile profile = PreRegistrationProfile.builder()
            .id(UUID.randomUUID())
            .user(user)
            .age(24)
            .phone("920000001")
            .parishOrArea("Norte")
            .hasMartialArtsExperience(true)
            .martialArtsExperienceDetails("Judo")
            .trainingGoal("Competicao")
            .preferredModality(PreRegistrationProfile.PreferredModality.JIU_JITSU)
            .preferredTrainingTime(PreRegistrationProfile.PreferredTrainingTime.NIGHT_AFTER_18)
            .preferredTrainingDays(Set.of(
                PreRegistrationProfile.PreferredTrainingDay.WEDNESDAY,
                PreRegistrationProfile.PreferredTrainingDay.MONDAY
            ))
            .valuesMartialArtsPhilosophy(true)
            .preferredContactMethod(PreRegistrationProfile.PreferredContactMethod.MESSAGE)
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(preRegistrationLeadRepository.findAllByStatusNotOrderBySubmittedAtDescIdDesc(eq("ARCHIVED"), any()))
            .thenReturn(new PageImpl<>(java.util.List.of(lead)));
        when(preRegistrationProfileRepository.findAllByOrderByCreatedAtDescIdDesc(any()))
            .thenReturn(new PageImpl<>(java.util.List.of(profile)));

        var result = adminService.getRegistrations(PageRequest.of(0, 20), "ALL");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(AdminRegistrationResponse::source)
            .containsExactly("SITE", "CSV");
        AdminRegistrationResponse site = result.getContent().get(0);
        assertThat(site.userId()).isEqualTo(user.getId());
        assertThat(site.leadId()).isNull();
        assertThat(site.email()).isEqualTo("site@test.com");
        assertThat(site.trainingGoal()).isEqualTo("Competicao");
        assertThat(site.preferredTrainingDays()).isEqualTo("MONDAY,WEDNESDAY");
        assertThat(site.status()).isEqualTo("REGISTERED");
        AdminRegistrationResponse csv = result.getContent().get(1);
        assertThat(csv.leadId()).isEqualTo(lead.getId());
        assertThat(csv.userId()).isNull();
        assertThat(csv.preferredModalities()).isEqualTo("Boxe");
        assertThat(csv.status()).isEqualTo("PRE_REGISTERED");
    }

    @Test
    void getStudentRegistrationProfile_looksUpProfileByUserId() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).name("Student").email("student@test.com").build();
        PreRegistrationProfile profile = PreRegistrationProfile.builder()
            .id(UUID.randomUUID())
            .user(user)
            .age(20)
            .phone("930000001")
            .parishOrArea("Sul")
            .hasMartialArtsExperience(false)
            .trainingGoal("Saude")
            .preferredModality(PreRegistrationProfile.PreferredModality.BOXE)
            .preferredTrainingTime(PreRegistrationProfile.PreferredTrainingTime.LUNCH_1230)
            .preferredTrainingDays(Set.of(PreRegistrationProfile.PreferredTrainingDay.FRIDAY))
            .valuesMartialArtsPhilosophy(false)
            .preferredContactMethod(PreRegistrationProfile.PreferredContactMethod.CALL)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        when(preRegistrationProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        Optional<AdminRegistrationResponse> result = adminService.getStudentRegistrationProfile(userId);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().userId()).isEqualTo(userId);
        assertThat(result.orElseThrow().trainingGoal()).isEqualTo("Saude");
    }

    @Test
    void getRegistrations_filtersBySourceBeforePagination() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 12, 0);
        PreRegistrationLead lead = leadWithId("00000000-0000-0000-0000-000000000010", now);
        User user = User.builder()
            .id(UUID.randomUUID())
            .name("Site Client")
            .email("site-filter@test.com")
            .build();
        PreRegistrationProfile profile = PreRegistrationProfile.builder()
            .id(UUID.randomUUID())
            .user(user)
            .age(24)
            .phone("920000001")
            .parishOrArea("Norte")
            .hasMartialArtsExperience(false)
            .trainingGoal("Saude")
            .preferredModality(PreRegistrationProfile.PreferredModality.JIU_JITSU)
            .preferredTrainingTime(PreRegistrationProfile.PreferredTrainingTime.NIGHT_AFTER_18)
            .preferredTrainingDays(Set.of(PreRegistrationProfile.PreferredTrainingDay.MONDAY))
            .valuesMartialArtsPhilosophy(true)
            .preferredContactMethod(PreRegistrationProfile.PreferredContactMethod.MESSAGE)
            .createdAt(now)
            .updatedAt(now)
            .build();
        PageRequest pageable = PageRequest.of(0, 1);
        when(preRegistrationProfileRepository.findAllByOrderByCreatedAtDescIdDesc(pageable))
            .thenReturn(new PageImpl<>(List.of(profile), pageable, 1));
        when(preRegistrationLeadRepository.findAllByStatusNotOrderBySubmittedAtDescIdDesc("ARCHIVED", pageable))
            .thenReturn(new PageImpl<>(List.of(lead), pageable, 1));

        var site = adminService.getRegistrations(pageable, "SITE");
        var csv = adminService.getRegistrations(pageable, "CSV");

        assertThat(site.getContent()).extracting(AdminRegistrationResponse::source).containsExactly("SITE");
        assertThat(site.getTotalElements()).isEqualTo(1);
        assertThat(csv.getContent()).extracting(AdminRegistrationResponse::source).containsExactly("CSV");
        assertThat(csv.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getRegistrations_keepsStableIdOrderingAcrossPageBoundaries() {
        LocalDateTime submittedAt = LocalDateTime.of(2026, 7, 20, 12, 0);
        List<PreRegistrationLead> leads = List.of(
            leadWithId("00000000-0000-0000-0000-000000000003", submittedAt),
            leadWithId("00000000-0000-0000-0000-000000000002", submittedAt),
            leadWithId("00000000-0000-0000-0000-000000000001", submittedAt)
        );
        when(preRegistrationLeadRepository.findAllByStatusNotOrderBySubmittedAtDescIdDesc(eq("ARCHIVED"), any()))
            .thenAnswer(invocation -> {
                PageRequest request = invocation.getArgument(1);
                return new PageImpl<>(leads.subList(0, Math.min(request.getPageSize(), leads.size())), request, leads.size());
            });
        lenient().when(preRegistrationProfileRepository.findAllByOrderByCreatedAtDescIdDesc(any()))
            .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(0), 0));

        var firstPage = adminService.getRegistrations(PageRequest.of(0, 1), "ALL");
        var secondPage = adminService.getRegistrations(PageRequest.of(1, 1), "ALL");

        assertThat(firstPage.getContent()).extracting(AdminRegistrationResponse::id)
            .containsExactly(UUID.fromString("00000000-0000-0000-0000-000000000003"));
        assertThat(secondPage.getContent()).extracting(AdminRegistrationResponse::id)
            .containsExactly(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    }

    private PreRegistrationLead leadWithId(String id, LocalDateTime submittedAt) {
        return PreRegistrationLead.builder()
            .id(UUID.fromString(id))
            .submittedAt(submittedAt)
            .fullName("Lead " + id)
            .phone("910000000")
            .source("GOOGLE_FORMS_IMPORT")
            .status("PRE_REGISTERED")
            .createdAt(submittedAt)
            .updatedAt(submittedAt)
            .build();
    }
}

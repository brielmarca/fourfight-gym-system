package com.gym.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.dto.response.PreRegistrationLeadImportResponse;
import com.gym.entity.PreRegistrationLead;
import com.gym.repository.AuditLogRepository;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PreRegistrationLeadRepository;
import com.gym.repository.PreRegistrationProfileRepository;
import com.gym.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AdminServicePreRegistrationImportTest {

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

        lenient().when(preRegistrationLeadRepository.findAllByStatusNotAndPhoneIsNotNull(anyString())).thenReturn(List.of());
        lenient().when(preRegistrationLeadRepository.existsByFullNameAndPhoneAndSubmittedAt(anyString(), anyString(), any(LocalDateTime.class)))
            .thenReturn(false);
        lenient().when(preRegistrationLeadRepository.save(any(PreRegistrationLead.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void importsFirstValidRow() {
        PreRegistrationLeadImportResponse response = adminService.importPreRegistrationsCsv(csvFile(
            header() +
            "01/01/2026 10:00:00,Joao Silva,20,923304078\n"
        ));

        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.importedRows()).isEqualTo(1);
        assertThat(response.duplicateRows()).isEqualTo(0);
        assertThat(response.invalidRows()).isEqualTo(0);
        verify(preRegistrationLeadRepository, times(1)).save(any(PreRegistrationLead.class));
    }

    @Test
    void skipsDuplicateSamePhoneWithinSameCsvEvenWithDifferentSubmittedAt() {
        PreRegistrationLeadImportResponse response = adminService.importPreRegistrationsCsv(csvFile(
            header() +
            "01/01/2026 10:00:00,Ana Silva,22,923304078\n" +
            "02/01/2026 11:15:30,Ana Silva,22,923304078\n"
        ));

        assertThat(response.totalRows()).isEqualTo(2);
        assertThat(response.importedRows()).isEqualTo(1);
        assertThat(response.duplicateRows()).isEqualTo(1);
        assertThat(response.issues()).anyMatch(msg -> msg.contains("Linha 3") && msg.contains("duplicado ignorado"));
        verify(preRegistrationLeadRepository, times(1)).save(any(PreRegistrationLead.class));
    }

    @Test
    void skipsDuplicateSamePhoneWithDifferentFormatting() {
        PreRegistrationLeadImportResponse response = adminService.importPreRegistrationsCsv(csvFile(
            header() +
            "01/01/2026 10:00:00,Pedro Costa,22,+351 923 304 078\n" +
            "02/01/2026 11:15:30,Pedro Costa,22,+351-923-304-078\n"
        ));

        assertThat(response.importedRows()).isEqualTo(1);
        assertThat(response.duplicateRows()).isEqualTo(1);
        verify(preRegistrationLeadRepository, times(1)).save(any(PreRegistrationLead.class));
    }

    @Test
    void skipsWhenExistingDatabaseLeadHasSameNormalizedPhone() {
        PreRegistrationLead existingLead = PreRegistrationLead.builder()
            .submittedAt(LocalDateTime.now().minusDays(1))
            .fullName("Existing")
            .phone("+351 923 304 078")
            .source("GOOGLE_FORMS_IMPORT")
            .status("PRE_REGISTERED")
            .build();
        when(preRegistrationLeadRepository.findAllByStatusNotAndPhoneIsNotNull(eq("ARCHIVED")))
            .thenReturn(List.of(existingLead));

        PreRegistrationLeadImportResponse response = adminService.importPreRegistrationsCsv(csvFile(
            header() +
            "01/01/2026 10:00:00,New Lead,21,+351923304078\n"
        ));

        assertThat(response.importedRows()).isEqualTo(0);
        assertThat(response.duplicateRows()).isEqualTo(1);
        verify(preRegistrationLeadRepository, never()).save(any(PreRegistrationLead.class));
    }

    @Test
    void marksMissingPhoneAsInvalid() {
        PreRegistrationLeadImportResponse response = adminService.importPreRegistrationsCsv(csvFile(
            header() +
            "01/01/2026 10:00:00,No Phone,21,\n"
        ));

        assertThat(response.importedRows()).isEqualTo(0);
        assertThat(response.invalidRows()).isEqualTo(1);
        verify(preRegistrationLeadRepository, never()).save(any(PreRegistrationLead.class));
    }

    @Test
    void importsSameNameWithDifferentPhones() {
        PreRegistrationLeadImportResponse response = adminService.importPreRegistrationsCsv(csvFile(
            header() +
            "01/01/2026 10:00:00,Nome Igual,20,923304078\n" +
            "01/01/2026 10:30:00,Nome Igual,20,923304079\n"
        ));

        assertThat(response.importedRows()).isEqualTo(2);
        assertThat(response.duplicateRows()).isEqualTo(0);
        verify(preRegistrationLeadRepository, times(2)).save(any(PreRegistrationLead.class));
    }

    @Test
    void skipsDifferentNamesWhenPhoneIsSameForSafety() {
        PreRegistrationLeadImportResponse response = adminService.importPreRegistrationsCsv(csvFile(
            header() +
            "01/01/2026 10:00:00,Carlos A,20,923304078\n" +
            "01/01/2026 10:30:00,Carlos B,20,923304078\n"
        ));

        assertThat(response.importedRows()).isEqualTo(1);
        assertThat(response.duplicateRows()).isEqualTo(1);
        verify(preRegistrationLeadRepository, times(1)).save(any(PreRegistrationLead.class));
    }

    @Test
    void keepsOriginalTrimmedPhoneWhenSaving() {
        adminService.importPreRegistrationsCsv(csvFile(
            header() +
            "01/01/2026 10:00:00,Phone Format,20, +351 923 304 078 \n"
        ));

        ArgumentCaptor<PreRegistrationLead> captor = ArgumentCaptor.forClass(PreRegistrationLead.class);
        verify(preRegistrationLeadRepository).save(captor.capture());
        assertThat(captor.getValue().getPhone()).isEqualTo("+351 923 304 078");
    }

    private MockMultipartFile csvFile(String content) {
        return new MockMultipartFile("file", "leads.csv", "text/csv", content.getBytes());
    }

    private String header() {
        return "Carimbo de data/hora,Nome completo,Idade,Telefone\n";
    }
}

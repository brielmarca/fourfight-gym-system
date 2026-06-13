package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.entity.AuditLog;
import com.gym.entity.Graduation;
import com.gym.entity.MartialArt;
import com.gym.entity.Membership;
import com.gym.entity.Plan;
import com.gym.entity.Student;
import com.gym.entity.StudentMartialArt;
import com.gym.entity.User;
import com.gym.repository.AuditLogRepository;
import com.gym.repository.GraduationRepository;
import com.gym.repository.MartialArtRepository;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.StudentMartialArtRepository;
import com.gym.repository.StudentRepository;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "rate-limit.login.capacity=100",
    "rate-limit.login.refill-tokens=100",
    "rate-limit.login.refill-duration=1"
})
@AutoConfigureMockMvc
class AdminStudentGraduationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private PlanRepository planRepository;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private MartialArtRepository martialArtRepository;
    @Autowired private GraduationRepository graduationRepository;
    @Autowired private StudentMartialArtRepository studentMartialArtRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    private User admin;
    private User manager;
    private User client;
    private Student student;
    private MartialArt jiuJitsu;
    private MartialArt boxeKickboxing;
    private Graduation whiteBelt;
    private Graduation blueBelt;
    private Graduation boxingBeginner;
    private StudentMartialArt jiuJitsuRecord;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        admin = saveUser("Graduation Admin " + suffix, "admin-" + suffix + "@fourfight.local", User.Role.ADMIN, true, null);
        manager = saveUser("Graduation Manager " + suffix, "manager-" + suffix + "@fourfight.local", User.Role.MANAGER, true, null);
        client = saveUser("Graduation Student " + suffix, "student-" + suffix + "@fourfight.local", User.Role.CLIENT, true, null);

        student = studentRepository.save(Student.builder()
            .name(client.getName())
            .email(client.getEmail())
            .isActive(true)
            .build());
        Plan plan = planRepository.save(Plan.builder()
            .name("Graduation Test Plan " + suffix)
            .description("Test plan")
            .price(new BigDecimal("49.00"))
            .currency("EUR")
            .durationDays(30)
            .isActive(true)
            .build());
        membershipRepository.save(Membership.builder()
            .user(client)
            .plan(plan)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .status(Membership.MembershipStatus.ACTIVE)
            .autoRenew(false)
            .build());

        jiuJitsu = findOrCreateMartialArt("Jiu-Jitsu");
        boxeKickboxing = findOrCreateMartialArt("Boxe / Kickboxing");
        MartialArt mma = findOrCreateMartialArt("MMA");

        whiteBelt = findOrCreateGraduation("Branca", 1, jiuJitsu);
        blueBelt = findOrCreateGraduation("Azul", 6, jiuJitsu);
        boxingBeginner = findOrCreateGraduation("Iniciante", 1, boxeKickboxing);
        findOrCreateGraduation("Fundamentos", 1, mma);

        jiuJitsuRecord = studentMartialArtRepository.save(StudentMartialArt.builder()
            .student(student)
            .martialArt(jiuJitsu)
            .graduation(whiteBelt)
            .startDate(LocalDate.now())
            .build());
    }

    @Test
    void adminCanUpdateStudentGraduationAndAuditIsWritten() throws Exception {
        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", client.getId())
                .header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "modality", "JIU_JITSU",
                    "graduationId", blueBelt.getId().toString(),
                    "reason", "Promocao aprovada pelo professor"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(client.getId().toString()))
            .andExpect(jsonPath("$.studentEmail").value(client.getEmail()))
            .andExpect(jsonPath("$.studentName").value(student.getName()))
            .andExpect(jsonPath("$.modality").value("JIU_JITSU"))
            .andExpect(jsonPath("$.oldGraduation").value("Branca"))
            .andExpect(jsonPath("$.newGraduation").value("Azul"));

        StudentMartialArt updated = studentMartialArtRepository.findById(jiuJitsuRecord.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updated.getGraduation().getId()).isEqualTo(blueBelt.getId());
        org.assertj.core.api.Assertions.assertThat(auditLogRepository.findAll().stream()
                .filter(log -> log.getEntityId().equals(jiuJitsuRecord.getId()))
                .map(AuditLog::getDiffJson)
                .anyMatch(diff -> diff.contains("Promocao aprovada pelo professor")
                    && diff.contains("Branca")
                    && diff.contains("Azul")))
            .isTrue();
    }

    @Test
    void clientCannotUpdateStudentGraduation() throws Exception {
        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", client.getId())
                .header("Authorization", bearer(client))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload(blueBelt.getId(), "JIU_JITSU")))
            .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotUpdateStudentGraduation() throws Exception {
        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", client.getId())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload(blueBelt.getId(), "JIU_JITSU")))
            .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotUpdateStudentGraduation() throws Exception {
        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", client.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload(blueBelt.getId(), "JIU_JITSU")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidGraduationIdIsRejected() throws Exception {
        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", client.getId())
                .header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload(UUID.randomUUID(), "JIU_JITSU")))
            .andExpect(status().isNotFound());
    }

    @Test
    void graduationFromWrongMartialArtIsRejected() throws Exception {
        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", client.getId())
                .header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload(boxingBeginner.getId(), "JIU_JITSU")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.graduationId").value("Graduation does not belong to selected modality"));
    }

    @Test
    void invalidModalityIsRejected() throws Exception {
        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", client.getId())
                .header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "modality", "INVALID",
                    "graduationId", blueBelt.getId().toString(),
                    "reason", "Invalid modality test"
                ))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void inactiveStudentIsRejected() throws Exception {
        User inactive = saveUser("Inactive Student", "inactive-" + UUID.randomUUID() + "@fourfight.local", User.Role.CLIENT, false, null);

        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", inactive.getId())
                .header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload(blueBelt.getId(), "JIU_JITSU")))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value("Student account is inactive"));
    }

    @Test
    void deactivatedStudentIsRejected() throws Exception {
        User deactivated = saveUser("Deactivated Student", "deactivated-" + UUID.randomUUID() + "@fourfight.local", User.Role.CLIENT, true, LocalDateTime.now());

        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", deactivated.getId())
                .header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload(blueBelt.getId(), "JIU_JITSU")))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value("Student account is deactivated"));
    }

    @Test
    void blankReasonIsRejected() throws Exception {
        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", client.getId())
                .header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "modality", "JIU_JITSU",
                    "graduationId", blueBelt.getId().toString(),
                    "reason", "   "
                ))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void boxeKickboxingMappingWorksWithSeededSpacedName() throws Exception {
        studentMartialArtRepository.save(StudentMartialArt.builder()
            .student(student)
            .martialArt(boxeKickboxing)
            .graduation(boxingBeginner)
            .startDate(LocalDate.now())
            .build());
        Graduation competition = findOrCreateGraduation("Competicao", 4, boxeKickboxing);

        mockMvc.perform(patch("/api/admin/students/{userId}/graduation", client.getId())
                .header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload(competition.getId(), "BOXE_KICKBOXING")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.modality").value("BOXE_KICKBOXING"))
            .andExpect(jsonPath("$.oldGraduation").value("Iniciante"))
            .andExpect(jsonPath("$.newGraduation").value("Competicao"));
    }

    @Test
    void existingAdminGraduationListStillWorks() throws Exception {
        mockMvc.perform(get("/api/admin/graduations")
                .header("Authorization", bearer(admin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].studentEmail").exists());
    }

    private String validPayload(UUID graduationId, String modality) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "modality", modality,
            "graduationId", graduationId.toString(),
            "reason", "Atualizacao administrativa validada"
        ));
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
    }

    private User saveUser(String name, String email, User.Role role, boolean active, LocalDateTime deactivatedAt) {
        return userRepository.save(User.builder()
            .name(name)
            .email(email)
            .passwordHash(passwordEncoder.encode("Pass12345!"))
            .role(role)
            .isActive(active)
            .deactivatedAt(deactivatedAt)
            .build());
    }

    private MartialArt findOrCreateMartialArt(String name) {
        return martialArtRepository.findByName(name)
            .orElseGet(() -> martialArtRepository.save(MartialArt.builder().name(name).build()));
    }

    private Graduation findOrCreateGraduation(String name, int levelOrder, MartialArt martialArt) {
        return graduationRepository.findByMartialArtId(martialArt.getId(), PageRequest.of(0, 100))
            .stream()
            .filter(graduation -> graduation.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> graduationRepository.save(Graduation.builder()
                .name(name)
                .levelOrder(levelOrder)
                .martialArt(martialArt)
                .build()));
    }
}

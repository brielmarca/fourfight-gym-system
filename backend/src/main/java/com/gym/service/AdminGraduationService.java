package com.gym.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gym.dto.request.AdminUpdateStudentGraduationByUserRequest;
import com.gym.dto.request.AdminUpdateStudentGraduationRequest;
import com.gym.dto.response.AdminGraduationOptionResponse;
import com.gym.dto.response.AdminStudentGraduationResponse;
import com.gym.dto.response.AdminStudentGraduationUpdateResponse;
import com.gym.entity.AuditLog;
import com.gym.entity.Graduation;
import com.gym.entity.MartialArt;
import com.gym.entity.Membership;
import com.gym.entity.Student;
import com.gym.entity.StudentMartialArt;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.repository.AuditLogRepository;
import com.gym.repository.GraduationRepository;
import com.gym.repository.MartialArtRepository;
import com.gym.repository.MembershipRepository;
import com.gym.repository.StudentMartialArtRepository;
import com.gym.repository.StudentRepository;
import com.gym.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminGraduationService {

    private static final List<String> TEST_MARKERS = List.of(
        "example.com",
        "postlive",
        "eurcheck",
        "payer",
        "payerx",
        "audit",
        "stripe.test",
        "livecheck",
        "program prune",
        "test"
    );

    private static final List<String> MODALITY_ORDER = List.of(
        "JIU_JITSU",
        "BOXE_KICKBOXING",
        "CAPOEIRA",
        "MMA"
    );

    private final MembershipRepository membershipRepository;
    private final StudentRepository studentRepository;
    private final MartialArtRepository martialArtRepository;
    private final GraduationRepository graduationRepository;
    private final StudentMartialArtRepository studentMartialArtRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminGraduationService(
        MembershipRepository membershipRepository,
        StudentRepository studentRepository,
        MartialArtRepository martialArtRepository,
        GraduationRepository graduationRepository,
        StudentMartialArtRepository studentMartialArtRepository,
        UserRepository userRepository,
        AuditLogRepository auditLogRepository,
        ObjectMapper objectMapper
    ) {
        this.membershipRepository = membershipRepository;
        this.studentRepository = studentRepository;
        this.martialArtRepository = martialArtRepository;
        this.graduationRepository = graduationRepository;
        this.studentMartialArtRepository = studentMartialArtRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AdminStudentGraduationResponse> listAdminGraduations() {
        List<Membership> activeMemberships = membershipRepository
            .findByStatusOrderByCreatedAtDesc(Membership.MembershipStatus.ACTIVE);

        Map<String, Membership> dedupedByEmail = new HashMap<>();
        for (Membership membership : activeMemberships) {
            String emailKey = normalize(membership.getUser().getEmail());
            if (emailKey.isBlank() || isTestRecord(membership)) {
                continue;
            }
            Membership existing = dedupedByEmail.get(emailKey);
            if (existing == null || membership.getCreatedAt().isAfter(existing.getCreatedAt())) {
                dedupedByEmail.put(emailKey, membership);
            }
        }

        List<StudentMartialArt> records = studentMartialArtRepository
            .findAllByStudentEmailIn(dedupedByEmail.keySet());

        List<AdminStudentGraduationResponse> response = new ArrayList<>();
        for (StudentMartialArt record : records) {
            Graduation current = record.getGraduation();
            String next = findNextGraduationName(record.getMartialArt().getId(), current.getLevelOrder());
            response.add(new AdminStudentGraduationResponse(
                record.getStudent().getName(),
                record.getStudent().getEmail(),
                toModalityKey(record.getMartialArt().getName()),
                current.getName(),
                next,
                record.getUpdatedAt()
            ));
        }

        response.sort(Comparator.comparing(AdminStudentGraduationResponse::studentName, String.CASE_INSENSITIVE_ORDER));
        return response;
    }

    @Transactional(readOnly = true)
    public List<AdminGraduationOptionResponse> listGraduationOptions() {
        return graduationRepository.findAll().stream()
            .map(graduation -> {
                MartialArt martialArt = graduation.getMartialArt();
                return new AdminGraduationOptionResponse(
                    graduation.getId(),
                    graduation.getName(),
                    graduation.getLevelOrder(),
                    toModalityKey(martialArt.getName()),
                    martialArt.getId(),
                    martialArt.getName()
                );
            })
            .sorted(Comparator
                .comparingInt((AdminGraduationOptionResponse option) -> modalitySortIndex(option.modality()))
                .thenComparing(AdminGraduationOptionResponse::levelOrder)
                .thenComparing(AdminGraduationOptionResponse::name, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    @Transactional
    public AdminStudentGraduationResponse updateStudentGraduation(AdminUpdateStudentGraduationRequest request) {
        String email = normalize(request.studentEmail());
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student", email));

        MartialArt martialArt = findMartialArtByModality(request.modality());

        Graduation graduation = graduationRepository
            .findByMartialArtId(martialArt.getId(), PageRequest.of(0, 100))
            .stream()
            .filter(g -> g.getName().equalsIgnoreCase(request.currentLevel().trim()))
            .findFirst()
            .orElseThrow(() -> new ValidationException("Graduação inválida para a modalidade selecionada"));

        StudentMartialArt record = studentMartialArtRepository
            .findByStudentIdAndMartialArtId(student.getId(), martialArt.getId())
            .orElseThrow(() -> new ResourceNotFoundException("StudentMartialArt", student.getId()));

        record.setGraduation(graduation);
        StudentMartialArt saved = studentMartialArtRepository.save(record);
        String next = findNextGraduationName(saved.getMartialArt().getId(), saved.getGraduation().getLevelOrder());

        return new AdminStudentGraduationResponse(
            saved.getStudent().getName(),
            saved.getStudent().getEmail(),
            request.modality(),
            saved.getGraduation().getName(),
            next,
            saved.getUpdatedAt()
        );
    }

    @Transactional
    public AdminStudentGraduationUpdateResponse updateStudentGraduation(
        UUID targetUserId,
        UUID actorUserId,
        AdminUpdateStudentGraduationByUserRequest request
    ) {
        String reason = request.reason() == null ? "" : request.reason().trim();
        if (reason.isBlank()) {
            throw new ValidationException(Map.of("reason", "Reason is required"));
        }
        if (reason.length() > 1000) {
            throw new ValidationException(Map.of("reason", "Reason must be at most 1000 characters"));
        }

        User actor = userRepository.findById(actorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", actorUserId));
        if (actor.getRole() != User.Role.ADMIN) {
            throw new BusinessRuleException("ADMIN_REQUIRED", "Only admins can update student graduation");
        }

        User target = userRepository.findById(targetUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));
        validateTargetStudent(target, targetUserId);

        MartialArt martialArt = findMartialArtByModality(request.modality());
        Graduation newGraduation = graduationRepository.findById(request.graduationId())
            .orElseThrow(() -> new ResourceNotFoundException("Graduation", request.graduationId()));
        if (!newGraduation.getMartialArt().getId().equals(martialArt.getId())) {
            throw new ValidationException(Map.of("graduationId", "Graduation does not belong to selected modality"));
        }

        Student student = studentRepository.findByEmailIgnoreCase(target.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Student", target.getEmail()));
        StudentMartialArt record = studentMartialArtRepository
            .findByStudentIdAndMartialArtId(student.getId(), martialArt.getId())
            .orElseThrow(() -> new ResourceNotFoundException("StudentMartialArt", student.getId()));

        Graduation oldGraduation = record.getGraduation();
        if (oldGraduation.getId().equals(newGraduation.getId())) {
            return toUpdateResponse(target, student, request.modality(), oldGraduation, newGraduation, record.getUpdatedAt());
        }

        record.setGraduation(newGraduation);
        StudentMartialArt saved = studentMartialArtRepository.save(record);
        auditLogRepository.save(AuditLog.builder()
            .actor(actor)
            .action(AuditLog.AuditAction.UPDATE)
            .entityType("StudentMartialArt")
            .entityId(saved.getId())
            .diffJson(buildGraduationAuditJson(actor, target, student, request.modality(), oldGraduation, newGraduation, reason))
            .build());

        return toUpdateResponse(target, student, request.modality(), oldGraduation, saved.getGraduation(), saved.getUpdatedAt());
    }

    private void validateTargetStudent(User target, UUID targetUserId) {
        if (target.getRole() != User.Role.CLIENT) {
            throw new BusinessRuleException("STUDENT_ONLY", "Only client student accounts can have graduation updated");
        }
        if (!Boolean.TRUE.equals(target.getIsActive())) {
            throw new BusinessRuleException("INACTIVE_STUDENT", "Student account is inactive");
        }
        if (target.getDeactivatedAt() != null) {
            throw new BusinessRuleException("DEACTIVATED_STUDENT", "Student account is deactivated");
        }
        if (target.getId() == null || !target.getId().equals(targetUserId)) {
            throw new ValidationException("Invalid target student");
        }
    }

    private AdminStudentGraduationUpdateResponse toUpdateResponse(
        User target,
        Student student,
        String modality,
        Graduation oldGraduation,
        Graduation newGraduation,
        java.time.LocalDateTime updatedAt
    ) {
        return new AdminStudentGraduationUpdateResponse(
            target.getId(),
            target.getEmail(),
            student.getName(),
            modality,
            oldGraduation.getName(),
            newGraduation.getName(),
            updatedAt
        );
    }

    private String buildGraduationAuditJson(
        User actor,
        User target,
        Student student,
        String modality,
        Graduation oldGraduation,
        Graduation newGraduation,
        String reason
    ) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("actorUserId", actor.getId().toString());
        root.put("targetUserId", target.getId().toString());
        root.put("studentId", student.getId().toString());
        root.put("studentEmail", target.getEmail());
        root.put("modality", modality);
        root.put("oldGraduationId", oldGraduation.getId().toString());
        root.put("oldGraduation", oldGraduation.getName());
        root.put("newGraduationId", newGraduation.getId().toString());
        root.put("newGraduation", newGraduation.getName());
        root.put("reason", reason);
        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to build graduation audit log", ex);
        }
    }

    private String findNextGraduationName(java.util.UUID martialArtId, int currentOrder) {
        return graduationRepository
            .findByMartialArtId(martialArtId, PageRequest.of(0, 100))
            .stream()
            .filter(g -> g.getLevelOrder() > currentOrder)
            .min(Comparator.comparingInt(Graduation::getLevelOrder))
            .map(Graduation::getName)
            .orElse("-");
    }

    private boolean isTestRecord(Membership membership) {
        String searchable = (membership.getUser().getName() + " " + membership.getUser().getEmail()).toLowerCase(Locale.ROOT);
        return TEST_MARKERS.stream().anyMatch(searchable::contains);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private MartialArt findMartialArtByModality(String modality) {
        for (String name : martialArtNamesForModality(modality)) {
            java.util.Optional<MartialArt> martialArt = martialArtRepository.findByName(name);
            if (martialArt.isPresent()) {
                return martialArt.get();
            }
        }
        throw new ValidationException("Modalidade inválida para graduação");
    }

    private List<String> martialArtNamesForModality(String modality) {
        return switch (modality) {
            case "JIU_JITSU" -> List.of("Jiu-Jitsu");
            case "BOXE_KICKBOXING" -> List.of("Boxe / Kickboxing", "Boxe/Kickboxing");
            case "CAPOEIRA" -> List.of("Capoeira");
            case "MMA" -> List.of("MMA");
            default -> throw new ValidationException("Modalidade inválida");
        };
    }

    private String toModalityKey(String martialArtName) {
        String normalized = martialArtName == null ? "" : martialArtName.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "jiu-jitsu" -> "JIU_JITSU";
            case "boxe/kickboxing", "boxe / kickboxing" -> "BOXE_KICKBOXING";
            case "capoeira" -> "CAPOEIRA";
            case "mma" -> "MMA";
            default -> martialArtName;
        };
    }

    private int modalitySortIndex(String modality) {
        int index = MODALITY_ORDER.indexOf(modality);
        return index >= 0 ? index : MODALITY_ORDER.size();
    }
}

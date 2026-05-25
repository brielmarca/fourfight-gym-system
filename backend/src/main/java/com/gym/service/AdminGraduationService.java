package com.gym.service;

import com.gym.dto.request.AdminUpdateStudentGraduationRequest;
import com.gym.dto.response.AdminStudentGraduationResponse;
import com.gym.entity.Graduation;
import com.gym.entity.MartialArt;
import com.gym.entity.Membership;
import com.gym.entity.Student;
import com.gym.entity.StudentMartialArt;
import com.gym.exception.ResourceNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.repository.GraduationRepository;
import com.gym.repository.MartialArtRepository;
import com.gym.repository.MembershipRepository;
import com.gym.repository.StudentMartialArtRepository;
import com.gym.repository.StudentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private final MembershipRepository membershipRepository;
    private final StudentRepository studentRepository;
    private final MartialArtRepository martialArtRepository;
    private final GraduationRepository graduationRepository;
    private final StudentMartialArtRepository studentMartialArtRepository;

    public AdminGraduationService(
        MembershipRepository membershipRepository,
        StudentRepository studentRepository,
        MartialArtRepository martialArtRepository,
        GraduationRepository graduationRepository,
        StudentMartialArtRepository studentMartialArtRepository
    ) {
        this.membershipRepository = membershipRepository;
        this.studentRepository = studentRepository;
        this.martialArtRepository = martialArtRepository;
        this.graduationRepository = graduationRepository;
        this.studentMartialArtRepository = studentMartialArtRepository;
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

    @Transactional
    public AdminStudentGraduationResponse updateStudentGraduation(AdminUpdateStudentGraduationRequest request) {
        String email = normalize(request.studentEmail());
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student", email));

        MartialArt martialArt = martialArtRepository.findByName(toMartialArtName(request.modality()))
            .orElseThrow(() -> new ValidationException("Modalidade inválida para graduação"));

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

    private String toMartialArtName(String modality) {
        return switch (modality) {
            case "JIU_JITSU" -> "Jiu-Jitsu";
            case "BOXE_KICKBOXING" -> "Boxe/Kickboxing";
            case "CAPOEIRA" -> "Capoeira";
            case "MMA" -> "MMA";
            default -> throw new ValidationException("Modalidade inválida");
        };
    }

    private String toModalityKey(String martialArtName) {
        String normalized = martialArtName == null ? "" : martialArtName.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "jiu-jitsu" -> "JIU_JITSU";
            case "boxe/kickboxing" -> "BOXE_KICKBOXING";
            case "capoeira" -> "CAPOEIRA";
            case "mma" -> "MMA";
            default -> martialArtName;
        };
    }
}

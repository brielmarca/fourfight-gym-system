package com.gym.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.gym.dto.response.AdminPreRegistrationLeadDetailResponse;
import com.gym.dto.response.AdminPreRegistrationLeadListItemResponse;
import com.gym.dto.response.AuditLogResponse;
import com.gym.dto.response.DashboardResponse;
import com.gym.dto.response.PreRegistrationLeadImportResponse;
import com.gym.dto.response.RevenueReportResponse.MonthlyRevenue;
import com.gym.dto.response.RevenueReportResponse;
import com.gym.entity.PreRegistrationLead;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.AuditLogRepository;
import com.gym.repository.PreRegistrationLeadRepository;
import com.gym.repository.PreRegistrationProfileRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MembershipService membershipService;
    private final UserService userService;
    private final TrainerService trainerService;
    private final ClassService classService;
    private final PaymentService paymentService;
    private final AuditLogRepository auditLogRepository;
    private final PreRegistrationProfileRepository preRegistrationProfileRepository;
    private final PreRegistrationLeadRepository preRegistrationLeadRepository;

    private static final String LEAD_SOURCE = "GOOGLE_FORMS_IMPORT";
    private static final String LEAD_STATUS = "PRE_REGISTERED";
    private static final String LEAD_STATUS_ACCEPTED = "ACCEPTED";
    private static final String LEAD_STATUS_ARCHIVED = "ARCHIVED";
    private static final DateTimeFormatter SUBMITTED_AT_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss");

    public DashboardResponse getDashboard() {
        long activeMembers = membershipService.countActive();
        long newMembersMTD = userService.countActiveClients();
        var revenueMTD = paymentService.getRevenueMTD() != null ? paymentService.getRevenueMTD() : java.math.BigDecimal.ZERO;
        
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        long classesToday = classService.getTodayClassCount(startOfDay, endOfDay);

        java.math.BigDecimal avgValue = java.math.BigDecimal.ZERO;
        if (activeMembers > 0 && revenueMTD.compareTo(java.math.BigDecimal.ZERO) > 0) {
            avgValue = revenueMTD.divide(java.math.BigDecimal.valueOf(activeMembers), 2, java.math.RoundingMode.HALF_UP);
        }

        return new DashboardResponse(
            activeMembers,
            0L,
            classesToday,
            newMembersMTD,
            avgValue,
            List.of(),
            List.of()
        );
    }

    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllOrderByCreatedAtDesc(pageable).map(AuditLogResponse::from);
    }

    public RevenueReportResponse getRevenueReport() {
        java.math.BigDecimal total = paymentService.getRevenueMTD();
        return new RevenueReportResponse(
            List.of(new RevenueReportResponse.MonthlyRevenue(LocalDateTime.now().getMonth().name(), total != null ? total : java.math.BigDecimal.ZERO)),
            total != null ? total : java.math.BigDecimal.ZERO,
            total != null ? total : java.math.BigDecimal.ZERO
        );
    }

    public Page<AdminPreRegistrationLeadListItemResponse> getPreRegistrations(Pageable pageable) {
        return preRegistrationLeadRepository.findAllByStatusNotOrderBySubmittedAtDesc(LEAD_STATUS_ARCHIVED, pageable)
            .map(AdminPreRegistrationLeadListItemResponse::from);
    }

    public AdminPreRegistrationLeadDetailResponse getPreRegistrationById(UUID id) {
        return preRegistrationLeadRepository.findById(id)
            .map(AdminPreRegistrationLeadDetailResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("PreRegistrationLead", id));
    }

    @Transactional
    public AdminPreRegistrationLeadDetailResponse acceptPreRegistration(UUID id) {
        PreRegistrationLead lead = preRegistrationLeadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PreRegistrationLead", id));

        lead.setStatus(LEAD_STATUS_ACCEPTED);
        return AdminPreRegistrationLeadDetailResponse.from(preRegistrationLeadRepository.save(lead));
    }

    @Transactional
    public AdminPreRegistrationLeadDetailResponse archivePreRegistration(UUID id) {
        PreRegistrationLead lead = preRegistrationLeadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PreRegistrationLead", id));

        lead.setStatus(LEAD_STATUS_ARCHIVED);
        return AdminPreRegistrationLeadDetailResponse.from(preRegistrationLeadRepository.save(lead));
    }

    @Transactional
    public PreRegistrationLeadImportResponse importPreRegistrationsCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new PreRegistrationLeadImportResponse(0, 0, 0, 1, List.of("Ficheiro CSV vazio."));
        }

        List<String> issues = new ArrayList<>();
        int totalRows = 0;
        int importedRows = 0;
        int duplicateRows = 0;
        int invalidRows = 0;

        try {
            String content = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            List<List<String>> rows = parseCsv(content);
            if (rows.size() <= 1) {
                return new PreRegistrationLeadImportResponse(0, 0, 0, 0, List.of("CSV sem linhas de dados."));
            }

            Set<String> existingNormalizedPhones = preRegistrationLeadRepository
                .findAllByStatusNotAndPhoneIsNotNull(LEAD_STATUS_ARCHIVED)
                .stream()
                .map(PreRegistrationLead::getPhone)
                .map(this::normalizePhone)
                .filter(normalized -> !normalized.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
            Set<String> importedNormalizedPhones = new HashSet<>();

            for (int i = 1; i < rows.size(); i++) {
                totalRows++;
                int lineNumber = i + 1;
                List<String> row = rows.get(i);
                try {
                    String submittedRaw = getCell(row, 0);
                    String fullName = getCell(row, 1);
                    String ageRaw = getCell(row, 2);
                    String phone = getCell(row, 3);
                    String normalizedFullName = normalizeName(fullName);
                    String normalizedPhone = normalizePhone(phone);

                    if (normalizedFullName.isBlank() || normalizedPhone.isBlank() || submittedRaw.isBlank()) {
                        invalidRows++;
                        issues.add("Linha " + lineNumber + ": campos obrigatorios em falta.");
                        continue;
                    }

                    if (importedNormalizedPhones.contains(normalizedPhone)) {
                        duplicateRows++;
                        issues.add("Linha " + lineNumber + ": duplicado ignorado por telefone já existente.");
                        continue;
                    }

                    if (existingNormalizedPhones.contains(normalizedPhone)) {
                        duplicateRows++;
                        issues.add("Linha " + lineNumber + ": duplicado ignorado por telefone já existente.");
                        importedNormalizedPhones.add(normalizedPhone);
                        continue;
                    }

                    LocalDateTime submittedAt = parseSubmittedAt(submittedRaw);
                    Integer age = parseAge(ageRaw);

                    if (preRegistrationLeadRepository.existsByFullNameAndPhoneAndSubmittedAt(fullName, phone, submittedAt)) {
                        duplicateRows++;
                        issues.add("Linha " + lineNumber + ": duplicado ignorado por telefone já existente.");
                        importedNormalizedPhones.add(normalizedPhone);
                        continue;
                    }

                    PreRegistrationLead lead = PreRegistrationLead.builder()
                        .submittedAt(submittedAt)
                        .fullName(fullName)
                        .age(age)
                        .phone(phone)
                        .parish(getCell(row, 4))
                        .hasMartialArtsExperience(parseBooleanPt(getCell(row, 5)))
                        .martialArtsExperienceDetails(getCell(row, 6))
                        .trainingGoal(getCell(row, 7))
                        .preferredModalities(getCell(row, 8))
                        .preferredTrainingTimes(getCell(row, 9))
                        .preferredTrainingDays(getCell(row, 10))
                        .philosophyImportant(parseBooleanPt(getCell(row, 11)))
                        .preferredContactMethod(getCell(row, 12))
                        .source(LEAD_SOURCE)
                        .status(LEAD_STATUS)
                        .notes(null)
                        .build();

                    preRegistrationLeadRepository.save(lead);
                    importedNormalizedPhones.add(normalizedPhone);
                    importedRows++;
                } catch (Exception e) {
                    invalidRows++;
                    issues.add("Linha " + lineNumber + ": " + sanitizeIssue(e.getMessage()));
                }
            }
        } catch (Exception e) {
            issues.add("Falha no processamento do CSV: " + sanitizeIssue(e.getMessage()));
            return new PreRegistrationLeadImportResponse(totalRows, importedRows, duplicateRows, invalidRows + 1, issues);
        }

        return new PreRegistrationLeadImportResponse(totalRows, importedRows, duplicateRows, invalidRows, issues);
    }

    private String getCell(List<String> row, int idx) {
        if (idx >= row.size()) return "";
        return row.get(idx) == null ? "" : row.get(idx).trim();
    }

    private LocalDateTime parseSubmittedAt(String raw) {
        try {
            return LocalDateTime.parse(raw.trim(), SUBMITTED_AT_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("data invalida no campo 'Carimbo de data/hora'");
        }
    }

    private Integer parseAge(String raw) {
        if (raw == null || raw.trim().isBlank()) return null;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("idade invalida");
        }
    }

    private Boolean parseBooleanPt(String raw) {
        if (raw == null || raw.trim().isBlank()) return null;
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("sim")) return true;
        if (normalized.equals("nao") || normalized.equals("não")) return false;
        return null;
    }

    private String sanitizeIssue(String message) {
        if (message == null || message.isBlank()) return "erro desconhecido";
        return message.replaceAll("[\\r\\n]", " ");
    }

    private String normalizePhone(String rawPhone) {
        if (rawPhone == null) return "";

        String trimmed = rawPhone.trim();
        if (trimmed.isBlank()) return "";

        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isDigit(c)) {
                normalized.append(c);
                continue;
            }
            if (c == '+' && normalized.length() == 0) {
                normalized.append(c);
            }
        }
        return normalized.toString();
    }

    private String normalizeName(String rawName) {
        if (rawName == null) return "";

        String collapsed = rawName.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        if (collapsed.isBlank()) return "";

        String decomposed = Normalizer.normalize(collapsed, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "");
    }

    private List<List<String>> parseCsv(String content) {
        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < content.length() && content.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                currentRow.add(cell.toString());
                cell.setLength(0);
            } else if ((c == '\n' || c == '\r') && !inQuotes) {
                if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') i++;
                currentRow.add(cell.toString());
                cell.setLength(0);
                if (!currentRow.stream().allMatch(String::isBlank)) {
                    rows.add(currentRow.stream().map(String::trim).collect(Collectors.toList()));
                }
                currentRow = new ArrayList<>();
            } else {
                cell.append(c);
            }
        }

        if (cell.length() > 0 || !currentRow.isEmpty()) {
            currentRow.add(cell.toString());
            if (!currentRow.stream().allMatch(String::isBlank)) {
                rows.add(currentRow.stream().map(String::trim).collect(Collectors.toList()));
            }
        }
        return rows;
    }
}

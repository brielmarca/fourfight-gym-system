package com.gym.dto.response;

import java.util.List;

public record PreRegistrationLeadImportResponse(
    int totalRows,
    int importedRows,
    int duplicateRows,
    int invalidRows,
    List<String> issues
) {}

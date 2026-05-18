package com.gym.dto.response;

import java.util.List;

public record MfaSetupResponse(
    String secret,
    String qrCodeUrl,
    List<String> backupCodes,
    String message
) {}
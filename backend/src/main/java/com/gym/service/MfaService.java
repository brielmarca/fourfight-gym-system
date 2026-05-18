package com.gym.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MfaService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String generateQrCodeUrl(String secret, String email) {
        return String.format("otpauth://totp/GymApp:%s?secret=%s&issuer=GymApp", email, secret);
    }

    public boolean verifyCode(String secret, String code) {
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            log.debug("TOTP verification failed: {}", e.getMessage());
            return false;
        }
    }

    public List<String> generateBackupCodes(int count) {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < count; i++) {
            int code = 10000000 + random.nextInt(90000000);
            codes.add(String.valueOf(code));
        }
        
        return codes;
    }

    public String hashBackupCodes(List<String> codes) {
        StringBuilder sb = new StringBuilder();
        for (String code : codes) {
            if (sb.length() > 0) sb.append(",");
            sb.append(hashCode(code));
        }
        return sb.toString();
    }

    public boolean verifyBackupCode(String hashedCodes, String providedCode) {
        String providedHash = hashCode(providedCode);
        
        if (hashedCodes == null) return false;
        
        for (String hashed : hashedCodes.split(",")) {
            if (hashed.equals(providedHash)) {
                return true;
            }
        }
        return false;
    }

    public String removeUsedBackupCode(String hashedCodes, String usedCode) {
        String usedHash = hashCode(usedCode);
        StringBuilder remaining = new StringBuilder();
        
        for (String hashed : hashedCodes.split(",")) {
            if (!hashed.equals(usedHash)) {
                if (remaining.length() > 0) remaining.append(",");
                remaining.append(hashed);
            }
        }
        
        return remaining.toString();
    }

    private String hashCode(String code) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(code.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash backup code", e);
        }
    }
}
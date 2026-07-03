package com.gym.service;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class MfaSecretEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int NONCE_LENGTH = 12;
    private static final String VERSION_PREFIX = "v1:";
    private static final String FIELD_SEPARATOR = ":";

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public MfaSecretEncryptionService(
            @Value("${mfa.encryption-key-base64:}") String encryptionKeyBase64,
            @Value("${spring.profiles.active:}") String activeProfiles) {
        String key = encryptionKeyBase64;
        if (key == null || key.isBlank()) {
            if (isProductionProfile(activeProfiles)) {
                throw new IllegalStateException(
                        "MFA_ENCRYPTION_KEY_BASE64 must be configured in production");
            }
            byte[] generated = new byte[32];
            secureRandom.nextBytes(generated);
            this.keySpec = new SecretKeySpec(generated, "AES");
            return;
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(key);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("MFA_ENCRYPTION_KEY_BASE64 is not valid Base64", e);
        }
        if (decoded.length != 32) {
            throw new IllegalStateException(
                    "MFA_ENCRYPTION_KEY_BASE64 must decode to exactly 32 bytes (256 bits), got "
                            + decoded.length);
        }
        this.keySpec = new SecretKeySpec(decoded, "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext must not be null");
        }
        byte[] nonce = new byte[NONCE_LENGTH];
        secureRandom.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return VERSION_PREFIX
                    + Base64.getEncoder().encodeToString(nonce)
                    + FIELD_SEPARATOR
                    + Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String storedValue) {
        if (storedValue == null) {
            throw new IllegalArgumentException("storedValue must not be null");
        }
        if (!isEncrypted(storedValue)) {
            throw new IllegalArgumentException("storedValue is not an encrypted payload");
        }
        try {
            String[] parts = storedValue.split(FIELD_SEPARATOR, 3);
            if (parts.length != 3) {
                throw new IllegalArgumentException("Malformed encrypted payload");
            }
            byte[] nonce = Base64.getDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[2]);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            throw new IllegalArgumentException("Decryption failed: data integrity check failed", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Decryption failed", e);
        }
    }

    public boolean isEncrypted(String storedValue) {
        return storedValue != null && storedValue.startsWith(VERSION_PREFIX);
    }

    private static boolean isProductionProfile(String profiles) {
        if (profiles == null || profiles.isBlank()) {
            return false;
        }
        for (String profile : profiles.split(",")) {
            String p = profile.trim();
            if ("prod".equalsIgnoreCase(p) || "production".equalsIgnoreCase(p)) {
                return true;
            }
        }
        return false;
    }
}

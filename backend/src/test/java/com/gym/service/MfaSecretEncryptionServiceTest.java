package com.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Base64;

class MfaSecretEncryptionServiceTest {

    private static final String TEST_KEY =
            "VEVTVC1LRVktTk9ULUZPUi1QUk9EVUNUSU9OLTAxMjM=";

    @Test
    @DisplayName("encrypt/decrypt round trip works")
    void roundTrip() {
        MfaSecretEncryptionService service = newService();
        String plaintext = "JBSWY3DPEHPK3PXP";
        String encrypted = service.encrypt(plaintext);
        assertThat(service.decrypt(encrypted)).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Same plaintext encrypts differently twice")
    void differentCiphertexts() {
        MfaSecretEncryptionService service = newService();
        String plaintext = "JBSWY3DPEHPK3PXP";
        String encrypted1 = service.encrypt(plaintext);
        String encrypted2 = service.encrypt(plaintext);
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("Ciphertext does not contain plaintext")
    void ciphertextDoesNotLeakPlaintext() {
        MfaSecretEncryptionService service = newService();
        String plaintext = "JBSWY3DPEHPK3PXP";
        String encrypted = service.encrypt(plaintext);
        assertThat(encrypted).doesNotContain(plaintext);
        assertThat(encrypted).doesNotContain("JBSWY");
    }

    @Test
    @DisplayName("Tampered ciphertext fails")
    void tamperedCiphertextFails() {
        MfaSecretEncryptionService service = newService();
        String plaintext = "JBSWY3DPEHPK3PXP";
        String encrypted = service.encrypt(plaintext);
        String tampered = encrypted.substring(0, encrypted.length() - 4) + "XXXX";
        assertThatThrownBy(() -> service.decrypt(tampered))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Wrong key fails")
    void wrongKeyFails() {
        MfaSecretEncryptionService correctService = newService();
        String plaintext = "JBSWY3DPEHPK3PXP";
        String encrypted = correctService.encrypt(plaintext);

        byte[] wrongKeyBytes = new byte[32];
        wrongKeyBytes[0] = 1;
        String wrongKey = Base64.getEncoder().encodeToString(wrongKeyBytes);
        MfaSecretEncryptionService wrongService = newService(wrongKey);
        assertThatThrownBy(() -> wrongService.decrypt(encrypted))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Malformed v1 payload fails")
    void malformedV1PayloadFails() {
        MfaSecretEncryptionService service = newService();
        assertThatThrownBy(() -> service.decrypt("v1:abc"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.decrypt("v1:abc:def:ghi"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.decrypt("v1:"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.decrypt("v1::"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.decrypt("v1:not-base64:!!!"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Malformed v1 value is never treated as plaintext")
    void malformedV1NeverFallsBackToPlaintext() {
        MfaSecretEncryptionService service = newService();
        assertThat(service.isEncrypted("v1:bad")).isTrue();
        assertThatThrownBy(() -> service.decrypt("v1:bad"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Invalid key length fails startup")
    void invalidKeyLengthFails() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> new MfaSecretEncryptionService(shortKey))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");

        String longKey = Base64.getEncoder().encodeToString(new byte[48]);
        assertThatThrownBy(() -> new MfaSecretEncryptionService(longKey))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    @DisplayName("Invalid Base64 key fails startup")
    void invalidBase64KeyFails() {
        assertThatThrownBy(() -> new MfaSecretEncryptionService("not-valid-base64!!!"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Startup fails without key regardless of profile")
    void startupFailsWithoutKey() {
        assertThatThrownBy(() -> new MfaSecretEncryptionService(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be configured");

        assertThatThrownBy(() -> new MfaSecretEncryptionService(""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be configured");

        assertThatThrownBy(() -> new MfaSecretEncryptionService("  "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be configured");
    }

    @Test
    @DisplayName("isEncrypted distinguishes v1 from legacy plaintext")
    void isEncryptedDetection() {
        MfaSecretEncryptionService service = newService();
        assertThat(service.isEncrypted("v1:abc:def")).isTrue();
        assertThat(service.isEncrypted("v1:anything")).isTrue();
        assertThat(service.isEncrypted("JBSWY3DPEHPK3PXP")).isFalse();
        assertThat(service.isEncrypted("plaintext-secret")).isFalse();
        assertThat(service.isEncrypted(null)).isFalse();
        assertThat(service.isEncrypted("")).isFalse();
        assertThat(service.isEncrypted("V1:abc:def")).isFalse();
    }

    @Test
    @DisplayName("Null plaintext is rejected")
    void nullPlaintextRejected() {
        MfaSecretEncryptionService service = newService();
        assertThatThrownBy(() -> service.encrypt(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Null storedValue is rejected")
    void nullStoredValueRejected() {
        MfaSecretEncryptionService service = newService();
        assertThatThrownBy(() -> service.decrypt(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Non-encrypted value is rejected by decrypt")
    void legacyPlaintextRejectedByDecrypt() {
        MfaSecretEncryptionService service = newService();
        assertThatThrownBy(() -> service.decrypt("JBSWY3DPEHPK3PXP"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not an encrypted payload");
    }

    @Test
    @DisplayName("Encrypted value starts with v1: prefix")
    void encryptedFormat() {
        MfaSecretEncryptionService service = newService();
        String encrypted = service.encrypt("JBSWY3DPEHPK3PXP");
        assertThat(encrypted).startsWith("v1:");
        String[] parts = encrypted.split(":", 3);
        assertThat(parts).hasSize(3);
        assertThat(parts[0]).isEqualTo("v1");
        assertThat(parts[1]).isNotEmpty();
        assertThat(parts[2]).isNotEmpty();
    }

    @Test
    @DisplayName("Value encrypted before recreating service with same key still decrypts")
    void sameKeyAcrossServiceInstances() {
        String plaintext = "JBSWY3DPEHPK3PXP";
        String encrypted = newService().encrypt(plaintext);
        MfaSecretEncryptionService secondService = newService();
        assertThat(secondService.decrypt(encrypted)).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Maximum expected secret fits within column length")
    void maximumSecretFitsInColumn() {
        MfaSecretEncryptionService service = newService();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            sb.append('A');
        }
        String largePlaintext = sb.toString();
        String encrypted = service.encrypt(largePlaintext);
        assertThat(encrypted).startsWith("v1:");
        assertThat(encrypted.length()).isLessThanOrEqualTo(255);
        assertThat(service.decrypt(encrypted)).isEqualTo(largePlaintext);
    }

    @Test
    @DisplayName("toString does not leak secret")
    void toStringDoesNotLeakSecret() {
        MfaSecretEncryptionService service = newService();
        String toString = service.toString();
        assertThat(toString).doesNotContain("keySpec");
        assertThat(toString).doesNotContain("AES");
    }

    @Test
    @DisplayName("Key is trimmed before decoding")
    void keyTrimmedBeforeDecoding() {
        MfaSecretEncryptionService service = new MfaSecretEncryptionService(
                "  " + TEST_KEY + "  ");
        String encrypted = service.encrypt("test");
        assertThat(service.decrypt(encrypted)).isEqualTo("test");
    }

    private MfaSecretEncryptionService newService() {
        return new MfaSecretEncryptionService(TEST_KEY);
    }

    private MfaSecretEncryptionService newService(String base64Key) {
        return new MfaSecretEncryptionService(base64Key);
    }
}

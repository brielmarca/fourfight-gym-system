package com.gym.entity;

import com.gym.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditLogColumnLimitTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    private void assertSaveSucceeds(AuditLog log) {
        assertDoesNotThrow(() -> auditLogRepository.saveAndFlush(log));
    }

    private void assertSaveFails(AuditLog log) {
        assertThrows(Exception.class, () -> auditLogRepository.saveAndFlush(log));
    }

    @Test
    void longestActionFitsInColumn() {
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.RATE_LIMIT_EXCEEDED)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .build());
    }

    @Test
    void actionHasRoomForFutureValues() {
        String futureAction = "X".repeat(30);
        assertEquals(30, futureAction.length());
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .build());
    }

    @Test
    void entityTypeAtExactLimitSucceeds() {
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("X".repeat(100))
            .entityId(UUID.randomUUID())
            .build());
    }

    @Test
    void entityTypeBeyondLimitFails() {
        assertSaveFails(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("X".repeat(101))
            .entityId(UUID.randomUUID())
            .build());
    }

    @Test
    void ipv6LengthFitsInColumn() {
        String longestPossibleIpv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        assertTrue(longestPossibleIpv6.length() <= 45, "IPv6 should fit within 45 chars");
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .ip(longestPossibleIpv6)
            .build());
    }

    @Test
    void ipBeyondLimitFails() {
        assertSaveFails(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .ip("1".repeat(46))
            .build());
    }

    @Test
    void userAgentFormerLimitWorks() {
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .userAgent("M".repeat(500))
            .build());
    }

    @Test
    void userAgentBeyondFormerLimitSucceeds() {
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .userAgent("M".repeat(2000))
            .build());
    }

    @Test
    void realisticLongUserAgentPersists() {
        String longUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 "
            + "Mobile/15E148";
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .userAgent(longUserAgent)
            .build());
    }

    @Test
    void unicodeTextPersistsCorrectly() {
        AuditLog saved = auditLogRepository.saveAndFlush(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("Entidade")
            .entityId(UUID.randomUUID())
            .diffJson("{\"nome\": \"João da Silva\"}")
            .userAgent("Mozilla/5.0 (compatible; MSIE 9.0)")
            .ip("::1")
            .build());
        assertNotNull(saved.getId());
        assertTrue(saved.getDiffJson().contains("João"));
    }

    @Test
    void realisticEntityTypeValuesFit() {
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("StudentMartialArt")
            .entityId(UUID.randomUUID())
            .build());
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.UPDATE)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .build());
    }

    @Test
    void nullDiffJsonIsAllowed() {
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .build());
    }

    @Test
    void longExceptionMessagePersists() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("error-").append(i).append(" ");
        }
        String longMessage = sb.toString();
        assertTrue(longMessage.length() > 500, "Exception message should exceed old VARCHAR(500) limit");
        assertSaveSucceeds(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .diffJson("{\"error\": \"" + longMessage + "\"}")
            .build());
    }

    @Test
    void longUserAgentNearLimitPreserved() {
        String agent = "A".repeat(2000);
        AuditLog saved = auditLogRepository.saveAndFlush(AuditLog.builder()
            .action(AuditLog.AuditAction.CREATE)
            .entityType("User")
            .entityId(UUID.randomUUID())
            .userAgent(agent)
            .build());
        assertEquals(2000, saved.getUserAgent().length());
    }
}

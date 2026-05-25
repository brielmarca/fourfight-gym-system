package com.gym.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "startup.diagnostics", name = "enabled", havingValue = "true")
public class DatabaseConnectionDiagnostics implements CommandLineRunner {

    private static final Pattern SUPABASE_HOST_PATTERN = Pattern.compile("jdbc:postgresql://([^/:?]+)");
    private static final Pattern PROJECT_REF_PATTERN = Pattern.compile("^db\\.([a-z0-9-]+)\\.supabase\\.co$");

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${SUPABASE_PROJECT_REF_EXPECTED:}")
    private String expectedProjectRef;

    @Override
    public void run(String... args) {
        String host = extractHost(datasourceUrl);
        String projectRef = extractProjectRef(host);
        boolean hasExpected = expectedProjectRef != null && !expectedProjectRef.isBlank();
        boolean matchesExpected = hasExpected && expectedProjectRef.equals(projectRef);

        log.info(
            "DB_DIAG hostPatternMatch={} projectRefPresent={} expectedConfigured={} expectedProjectMatch={}",
            host != null,
            projectRef != null,
            hasExpected,
            matchesExpected
        );
    }

    private String extractHost(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return null;
        }
        Matcher matcher = SUPABASE_HOST_PATTERN.matcher(jdbcUrl);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private String extractProjectRef(String host) {
        if (host == null) {
            return null;
        }
        Matcher matcher = PROJECT_REF_PATTERN.matcher(host);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }
}

package com.gym.tools;

import com.gym.entity.User;
import com.gym.repository.UserRepository;
import com.gym.service.AuthService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("admin-unlock")
@RequiredArgsConstructor
public class AdminUnlockRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    public void run(String... args) {
        String email = normalizeEmail(envOrBlank("ADMIN_UNLOCK_EMAIL"));
        String confirm = envOrBlank("CONFIRM_ADMIN_UNLOCK");

        if (email.isBlank() || !"true".equalsIgnoreCase(confirm)) {
            System.err.println("Required env vars: ADMIN_UNLOCK_EMAIL and CONFIRM_ADMIN_UNLOCK=true");
            System.exit(1);
        }

        User admin = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            System.err.println("ADMIN account not found for provided email.");
            System.exit(1);
        }

        boolean cleared = authService.unlockAccountLockout(email);
        String status = cleared ? "cleared" : "already clear";
        System.out.println("ADMIN lockout state " + status + " for email=" + admin.getEmail());
        System.exit(0);
    }

    private String envOrBlank(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value.trim();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

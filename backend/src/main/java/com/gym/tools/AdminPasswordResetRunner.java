package com.gym.tools;

import com.gym.entity.User;
import com.gym.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("password-reset")
@RequiredArgsConstructor
public class AdminPasswordResetRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String listOnly = envOrBlank("LIST_ADMINS_ONLY");
        if ("true".equalsIgnoreCase(listOnly)) {
            listAdmins();
            System.exit(0);
        }

        String adminEmail = envOrBlank("ADMIN_EMAIL");
        String newPassword = envOrBlank("ADMIN_NEW_PASSWORD");

        if (adminEmail.isBlank() || newPassword.isBlank()) {
            System.err.println("Required env vars: ADMIN_EMAIL and ADMIN_NEW_PASSWORD");
            System.exit(1);
        }

        validatePassword(newPassword);

        User user = userRepository.findByEmail(adminEmail).orElse(null);
        if (user == null || user.getRole() != User.Role.ADMIN) {
            System.err.println("Admin account not found for provided email.");
            System.exit(1);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        System.out.println("Password reset completed for ADMIN account: " + safeEmail(user.getEmail()));
        System.out.println("User id: " + user.getId());
        System.exit(0);
    }

    private void listAdmins() {
        List<User> admins = userRepository.findAll().stream()
            .filter(user -> user.getRole() == User.Role.ADMIN)
            .toList();

        if (admins.isEmpty()) {
            System.out.println("No ADMIN users found.");
            return;
        }

        System.out.println("ADMIN users:");
        for (User admin : admins) {
            System.out.println(
                "id=" + admin.getId()
                    + " name=" + admin.getName()
                    + " email=" + safeEmail(admin.getEmail())
                    + " role=" + admin.getRole().name()
            );
        }
    }

    private String envOrBlank(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value.trim();
    }

    private void validatePassword(String password) {
        if (password.length() < 8 || password.length() > 100) {
            throw new IllegalArgumentException("Password must be between 8 and 100 characters.");
        }
        if (!password.chars().anyMatch(Character::isUpperCase)) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        }
        if (!password.chars().anyMatch(Character::isLowerCase)) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
        }
        if (!password.chars().anyMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Password must contain at least one digit.");
        }
    }

    private String safeEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}

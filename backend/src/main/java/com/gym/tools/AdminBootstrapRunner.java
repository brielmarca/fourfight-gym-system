package com.gym.tools;

import com.gym.entity.User;
import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("admin-bootstrap")
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String confirm = envOrBlank("CONFIRM_CREATE_ADMIN");
        String name = envOrBlank("ADMIN_NAME");
        String email = envOrBlank("ADMIN_EMAIL");
        String password = envOrBlank("ADMIN_NEW_PASSWORD");

        if (!"true".equalsIgnoreCase(confirm)) {
            System.err.println("Required env var: CONFIRM_CREATE_ADMIN=true");
            System.exit(1);
        }

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            System.err.println("Required env vars: ADMIN_NAME, ADMIN_EMAIL, ADMIN_NEW_PASSWORD");
            System.exit(1);
        }

        validatePassword(password);

        if (userRepository.findByEmail(email).isPresent()) {
            System.err.println("User already exists for provided email. Use promote-admin and reset-admin-password tools.");
            System.exit(1);
        }

        User admin = User.builder()
            .name(name)
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .role(User.Role.ADMIN)
            .isActive(true)
            .build();

        User saved = userRepository.save(admin);
        System.out.println("ADMIN bootstrap completed.");
        System.out.println(
            "id=" + saved.getId()
                + " name=" + safe(saved.getName())
                + " email=" + saved.getEmail()
                + " role=" + saved.getRole().name()
        );
        System.exit(0);
    }

    private String envOrBlank(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
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
}

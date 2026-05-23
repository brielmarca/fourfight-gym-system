package com.gym.tools;

import com.gym.entity.User;
import com.gym.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("admin-promote")
@RequiredArgsConstructor
public class AdminPromoteRunner implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        String listOnly = envOrBlank("LIST_USERS_ONLY");
        if ("true".equalsIgnoreCase(listOnly)) {
            listUsers();
            System.exit(0);
        }

        String email = envOrBlank("PROMOTE_ADMIN_EMAIL");
        String confirm = envOrBlank("CONFIRM_PROMOTE_ADMIN");
        if (email.isBlank() || !"true".equalsIgnoreCase(confirm)) {
            System.err.println("Required env vars: PROMOTE_ADMIN_EMAIL and CONFIRM_PROMOTE_ADMIN=true");
            System.exit(1);
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            System.err.println("User not found for provided email.");
            System.exit(1);
        }

        if (user.getRole() != User.Role.ADMIN) {
            user.setRole(User.Role.ADMIN);
            userRepository.save(user);
        }

        System.out.println("ADMIN promotion completed.");
        System.out.println(
            "id=" + user.getId()
                + " name=" + safe(user.getName())
                + " email=" + user.getEmail()
                + " role=" + user.getRole().name()
        );
        System.exit(0);
    }

    private void listUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println("Users:");
        for (User user : users) {
            System.out.println(
                "id=" + user.getId()
                    + " name=" + safe(user.getName())
                    + " email=" + user.getEmail()
                    + " role=" + user.getRole().name()
            );
        }
    }

    private String envOrBlank(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}

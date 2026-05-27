package com.gym.service;

import com.gym.entity.PasswordResetToken;
import com.gym.entity.User;
import com.gym.exception.ValidationException;
import com.gym.repository.PasswordResetTokenRepository;
import com.gym.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.password-reset-path:/redefinir-senha}")
    private String passwordResetPath;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from-email:no-reply@example.com}")
    private String resendFromEmail;

    @Value("${app.password-reset-expiration-minutes:30}")
    private long resetExpirationMinutes;

    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmailIgnoreCase(email)
            .ifPresent(this::createTokenAndSendEmail);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hashToken(rawToken))
            .orElseThrow(() -> new ValidationException(Map.of("token", "Token invalido ou expirado.")));

        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException(Map.of("token", "Token invalido ou expirado."));
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);

        passwordResetTokenRepository.findAllByUserIdAndUsedAtIsNullAndExpiresAtAfter(user.getId(), LocalDateTime.now())
            .forEach(other -> {
                other.setUsedAt(LocalDateTime.now());
                passwordResetTokenRepository.save(other);
            });
    }

    private void createTokenAndSendEmail(User user) {
        String rawToken = generateRawToken();
        PasswordResetToken token = PasswordResetToken.builder()
            .user(user)
            .tokenHash(hashToken(rawToken))
            .expiresAt(LocalDateTime.now().plusMinutes(resetExpirationMinutes))
            .usedAt(null)
            .build();
        passwordResetTokenRepository.save(token);
        sendResetEmail(user.getEmail(), rawToken);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    private void sendResetEmail(String to, String rawToken) {
        String resetLink = frontendUrl.replaceAll("/+$", "") + passwordResetPath + "?token=" + rawToken;

        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("Password reset requested but RESEND_API_KEY not configured. email={}", to);
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);
            String body = "{" +
                "\"from\":\"" + resendFromEmail + "\"," +
                "\"to\":[\"" + to + "\"]," +
                "\"subject\":\"Recuperacao de palavra-passe\"," +
                "\"html\":\"<p>Recebemos um pedido para redefinir a tua palavra-passe.</p><p><a href='" + resetLink + "'>Redefinir palavra-passe</a></p><p>Se nao pediste, ignora este email.</p>\"" +
                "}";
            restTemplate.postForEntity("https://api.resend.com/emails", new HttpEntity<>(body, headers), String.class);
        } catch (Exception ex) {
            log.error("Failed to send password reset email", ex);
        }
    }
}

package com.gym.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.LoginRequest;
import com.gym.dto.request.PreferredContactMethod;
import com.gym.dto.request.PreferredModality;
import com.gym.dto.request.PreferredTrainingDay;
import com.gym.dto.request.PreferredTrainingTime;
import com.gym.dto.request.RegisterRequest;
import com.gym.dto.response.TokenPairResponse;
import com.gym.dto.response.UserResponse;
import com.gym.entity.PreRegistrationProfile;
import com.gym.entity.RefreshToken;
import com.gym.entity.User.Role;
import com.gym.entity.User;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.exception.UnauthorizedException;
import com.gym.repository.RefreshTokenRepository;
import com.gym.repository.PreRegistrationProfileRepository;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PreRegistrationProfileRepository preRegistrationProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${security.lockout.failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lockout.duration:PT15M}")
    private String lockoutDuration;

    @Value("${AUTH_DIAGNOSTICS_ENABLED:false}")
    private boolean authDiagnosticsEnabled;

    private static final Map<String, Integer> failedLoginAttempts = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> lockedAccounts = new java.util.concurrent.ConcurrentHashMap<>();

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        User user = User.builder()
            .name(request.name())
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(request.password()))
            .phone(request.phone())
            .dateOfBirth(request.dateOfBirth())
            .role(User.Role.CLIENT)
            .isActive(true)
            .build();

        user = userRepository.save(user);

        PreRegistrationProfile profile = PreRegistrationProfile.builder()
            .user(user)
            .age(request.age())
            .phone(request.phone())
            .parishOrArea(request.parishOrArea())
            .hasMartialArtsExperience(request.hasMartialArtsExperience())
            .martialArtsExperienceDetails(request.martialArtsExperienceDetails())
            .trainingGoal(request.trainingGoal())
            .preferredModality(mapPreferredModality(request.preferredModality()))
            .preferredModalityOther(request.preferredModalityOther())
            .preferredTrainingTime(mapPreferredTrainingTime(request.preferredTrainingTime()))
            .preferredTrainingTimeOther(request.preferredTrainingTimeOther())
            .preferredTrainingDays(mapPreferredTrainingDays(request.preferredTrainingDays()))
            .valuesMartialArtsPhilosophy(request.valuesMartialArtsPhilosophy())
            .preferredContactMethod(mapPreferredContactMethod(request.preferredContactMethod()))
            .preferredContactMethodOther(request.preferredContactMethodOther())
            .build();

        preRegistrationProfileRepository.save(profile);
        log.info("New user registered: {}", user.getEmail());

        return UserResponse.from(user);
    }

    @Transactional
    public TokenPairResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String clientIp = getClientIp();
        String loginStatus = "UNAUTHORIZED";
        String role = "N/A";
        boolean userFound = false;
        boolean userActive = false;
        boolean deletedAtNull = false;
        boolean passwordMatch = false;
        
        if (isAccountLocked(normalizedEmail)) {
            log.warn("Login attempt on locked account: {} from IP: {}", normalizedEmail, clientIp);
            loginStatus = "LOCKED";
            logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, passwordMatch, loginStatus);
            throw new UnauthorizedException("Account temporarily locked due to too many failed attempts");
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(() -> userRepository.findByEmail(normalizedEmail).orElse(null));
        if (user == null) {
            logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, passwordMatch, loginStatus);
            throw UnauthorizedException.invalidCredentials();
        }
        userFound = true;
        userActive = Boolean.TRUE.equals(user.getIsActive());
        deletedAtNull = user.getDeletedAt() == null;
        role = user.getRole().name();

        passwordMatch = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (!passwordMatch) {
            handleFailedLogin(normalizedEmail, clientIp);
            logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, passwordMatch, loginStatus);
            throw UnauthorizedException.invalidCredentials();
        }

        if (!user.getIsActive()) {
            loginStatus = "INACTIVE";
            logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, passwordMatch, loginStatus);
            throw new UnauthorizedException("Account is disabled");
        }

        clearFailedLoginAttempts(normalizedEmail);
        
        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            loginStatus = "MFA_PREAUTH";
            logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, passwordMatch, loginStatus);
            return generatePreAuthToken(user);
        }
        
        loginStatus = "SUCCESS";
        logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, passwordMatch, loginStatus);
        return generateTokenPair(user);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void logAuthDiagnostics(
        String normalizedEmail,
        boolean userFound,
        boolean userActive,
        boolean deletedAtNull,
        String role,
        boolean passwordMatch,
        String loginStatus
    ) {
        if (!authDiagnosticsEnabled) {
            return;
        }
        log.info(
            "AUTH_DIAG email={} userFound={} userActive={} deletedAtNull={} role={} passwordMatch={} loginStatus={}",
            normalizedEmail,
            userFound,
            userActive,
            deletedAtNull,
            role,
            passwordMatch,
            loginStatus
        );
    }

    public TokenPairResponse generatePreAuthToken(User user) {
        String preAuthToken = jwtUtil.generatePreAuthToken(user.getId(), user.getEmail());
        
        log.info("Pre-auth token generated for MFA user: {}", user.getEmail());
        
        return TokenPairResponse.of(preAuthToken, null, 300000);
    }

    public TokenPairResponse generateTokensForUser(User user) {
        return generateTokenPair(user);
    }

    private void handleFailedLogin(String email, String clientIp) {
        int attempts = failedLoginAttempts.getOrDefault(email, 0) + 1;
        failedLoginAttempts.put(email, attempts);
        
        log.warn("Failed login attempt {} of {} for user {} from IP: {}", 
            attempts, maxFailedAttempts, email, clientIp);
        
        if (attempts >= maxFailedAttempts) {
            lockAccount(email);
            log.warn("Account locked due to {} failed attempts: {}", attempts, email);
        }
    }

    private void lockAccount(String email) {
        lockedAccounts.put(email, LocalDateTime.now().plus(Duration.parse(lockoutDuration)));
    }

    private boolean isAccountLocked(String email) {
        LocalDateTime lockExpiresAt = lockedAccounts.get(email);
        if (lockExpiresAt == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(lockExpiresAt)) {
            lockedAccounts.remove(email);
            failedLoginAttempts.remove(email);
            log.info("Account lock expired for user: {}", email);
            return false;
        }
        
        return true;
    }

    private void clearFailedLoginAttempts(String email) {
        failedLoginAttempts.remove(email);
    }

    @Transactional
    public TokenPairResponse refresh(String rawRefreshToken) {
        if (!jwtUtil.validateToken(rawRefreshToken)) {
            throw UnauthorizedException.tokenExpired();
        }

        String tokenType = jwtUtil.extractClaim(rawRefreshToken, claims -> claims.get("typ", String.class));
        if (!"rt+jwt".equals(tokenType)) {
            throw UnauthorizedException.tokenExpired();
        }

        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findValidByTokenHash(tokenHash)
            .orElseThrow(UnauthorizedException::tokenExpired);

        User user = refreshToken.getUser();
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        revokeAllUserTokens(user.getId());

        log.info("Refresh token rotated for user: {}", user.getEmail());
        return generateTokenPair(user);
    }

    @Transactional
    public void logout(UUID userId) {
        revokeAllUserTokens(userId);
        log.info("User logged out: {}", userId);
    }

    @Transactional
    public void logoutByRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findValidByTokenHash(tokenHash)
            .ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                log.info("Refresh token revoked during logout fallback for userId={}", token.getUser().getId());
            });
    }

    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserResponse.from(user);
    }

    private TokenPairResponse generateTokenPair(User user) {
        revokeAllUserTokens(user.getId());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken token = RefreshToken.builder()
            .user(user)
            .tokenHash(hashToken(refreshToken))
            .expiresAt(LocalDateTime.now().plusNanos(jwtUtil.getRefreshExpirationMs() * 1_000_000))
            .revoked(false)
            .build();

        refreshTokenRepository.save(token);
        log.info("Token pair generated for user: {}", user.getEmail());

        return TokenPairResponse.of(accessToken, refreshToken, jwtUtil.getExpirationMs());
    }

    private void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.findValidByUserId(userId)
            .ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String getClientIp() {
        return "127.0.0.1";
    }

    private PreRegistrationProfile.PreferredModality mapPreferredModality(PreferredModality value) {
        return PreRegistrationProfile.PreferredModality.valueOf(value.name());
    }

    private PreRegistrationProfile.PreferredTrainingTime mapPreferredTrainingTime(PreferredTrainingTime value) {
        return PreRegistrationProfile.PreferredTrainingTime.valueOf(value.name());
    }

    private java.util.Set<PreRegistrationProfile.PreferredTrainingDay> mapPreferredTrainingDays(java.util.List<PreferredTrainingDay> values) {
        return values.stream()
            .map(value -> PreRegistrationProfile.PreferredTrainingDay.valueOf(value.name()))
            .collect(java.util.stream.Collectors.toSet());
    }

    private PreRegistrationProfile.PreferredContactMethod mapPreferredContactMethod(PreferredContactMethod value) {
        return PreRegistrationProfile.PreferredContactMethod.valueOf(value.name());
    }
}

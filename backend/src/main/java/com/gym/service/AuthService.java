package com.gym.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Map;
import java.util.UUID;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gym.config.LoginLockoutProperties;
import com.gym.security.ClientIpResolver;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
import com.gym.exception.ValidationException;
import com.gym.repository.RefreshTokenRepository;
import com.gym.repository.PreRegistrationProfileRepository;
import com.gym.repository.UserRepository;
import com.gym.security.JwtUtil;
import com.gym.util.InputSanitizer;
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
    private final LoginLockoutProperties loginLockoutProperties;

    @Value("${AUTH_DIAGNOSTICS_ENABLED:false}")
    private boolean authDiagnosticsEnabled;

    @Getter
    private static class LoginAttemptState {
        private final int failedAttempts;
        private final Instant lockedUntil;

        LoginAttemptState(int failedAttempts, Instant lockedUntil) {
            this.failedAttempts = failedAttempts;
            this.lockedUntil = lockedUntil;
        }

        LoginAttemptState withIncrement() {
            return new LoginAttemptState(failedAttempts + 1, lockedUntil);
        }

        LoginAttemptState withLock(Instant expiry) {
            return new LoginAttemptState(failedAttempts, expiry);
        }

        boolean isLocked(Instant now) {
            return lockedUntil != null && now.isBefore(lockedUntil);
        }
    }

    private Cache<String, LoginAttemptState> loginAttemptCache;

    @PostConstruct
    void initLoginAttemptCache() {
        this.loginAttemptCache = Caffeine.newBuilder()
            .maximumSize(loginLockoutProperties.cacheMaxSize())
            .expireAfterWrite(loginLockoutProperties.cacheExpiry())
            .build();
    }

    private Clock clock = Clock.systemUTC();

    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Autowired
    private ClientIpResolver clientIpResolver;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        int calculatedAge = validateAndCalculateAge(request.dateOfBirth());
        validateClientAgeConsistency(request.age(), calculatedAge);

        User user = User.builder()
            .name(InputSanitizer.trimToNull(request.name()))
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(request.password()))
            .phone(InputSanitizer.trimToNull(request.phone()))
            .dateOfBirth(request.dateOfBirth())
            .role(User.Role.CLIENT)
            .isActive(true)
            .build();

        user = userRepository.save(user);

        PreRegistrationProfile profile = PreRegistrationProfile.builder()
            .user(user)
            .age(calculatedAge)
            .phone(InputSanitizer.trimToNull(request.phone()))
            .parishOrArea(InputSanitizer.trimToNull(request.parishOrArea()))
            .hasMartialArtsExperience(request.hasMartialArtsExperience())
            .martialArtsExperienceDetails(InputSanitizer.trimToNull(request.martialArtsExperienceDetails()))
            .trainingGoal(InputSanitizer.trimToNull(request.trainingGoal()))
            .preferredModality(mapPreferredModality(request.preferredModality()))
            .preferredModalityOther(InputSanitizer.trimToNull(request.preferredModalityOther()))
            .preferredTrainingTime(mapPreferredTrainingTime(request.preferredTrainingTime()))
            .preferredTrainingTimeOther(InputSanitizer.trimToNull(request.preferredTrainingTimeOther()))
            .preferredTrainingDays(mapPreferredTrainingDays(request.preferredTrainingDays()))
            .valuesMartialArtsPhilosophy(request.valuesMartialArtsPhilosophy())
            .preferredContactMethod(mapPreferredContactMethod(request.preferredContactMethod()))
            .preferredContactMethodOther(InputSanitizer.trimToNull(request.preferredContactMethodOther()))
            .build();

        preRegistrationProfileRepository.save(profile);
        log.info("New user registered: {}", user.getEmail());

        return UserResponse.from(user);
    }

    private int validateAndCalculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new ValidationException(Map.of("dateOfBirth", "Date of birth is required"));
        }

        LocalDate today = LocalDate.now();
        if (dateOfBirth.isAfter(today)) {
            throw new ValidationException(Map.of("dateOfBirth", "Date of birth must be in the past"));
        }

        int calculatedAge = Period.between(dateOfBirth, today).getYears();
        if (calculatedAge < 3 || calculatedAge > 100) {
            throw new ValidationException(Map.of("dateOfBirth", "Age must be between 3 and 100 years"));
        }

        return calculatedAge;
    }

    private void validateClientAgeConsistency(Integer clientAge, int calculatedAge) {
        if (clientAge == null) {
            throw new ValidationException(Map.of("age", "Age is required"));
        }
        if (!clientAge.equals(calculatedAge)) {
            throw new ValidationException(Map.of("age", "Age does not match date of birth"));
        }
    }

    @Transactional
    public TokenPairResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw UnauthorizedException.invalidCredentials();
        }
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
            logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, loginStatus);
            throw UnauthorizedException.invalidCredentials();
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(() -> userRepository.findByEmail(normalizedEmail).orElse(null));
        if (user == null) {
            logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, loginStatus);
            throw UnauthorizedException.invalidCredentials();
        }
        userFound = true;
        userActive = Boolean.TRUE.equals(user.getIsActive());
        deletedAtNull = user.getDeletedAt() == null;
        role = user.getRole().name();

        passwordMatch = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (!passwordMatch) {
            handleFailedLogin(normalizedEmail, clientIp);
            logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, loginStatus);
            throw UnauthorizedException.invalidCredentials();
        }

        if (!user.getIsActive()) {
            loginStatus = "INACTIVE";
            logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, loginStatus);
            throw UnauthorizedException.invalidCredentials();
        }

        clearFailedLoginAttempts(normalizedEmail);
        
        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            loginStatus = "MFA_PREAUTH";
            logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, loginStatus);
            return generatePreAuthToken(user);
        }
        
        loginStatus = "SUCCESS";
        logAuthDiagnostics(normalizedEmail, userFound, userActive, deletedAtNull, role, loginStatus);
        return generateTokenPair(user);
    }

    private String normalizeEmail(String email) {
        return InputSanitizer.normalizeEmail(email);
    }

    private void logAuthDiagnostics(
        String normalizedEmail,
        boolean userFound,
        boolean userActive,
        boolean deletedAtNull,
        String role,
        String loginStatus
    ) {
        if (!authDiagnosticsEnabled) {
            return;
        }
        log.info(
            "AUTH_DIAG email={} userFound={} userActive={} deletedAtNull={} role={} loginStatus={}",
            normalizedEmail,
            userFound,
            userActive,
            deletedAtNull,
            role,
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
        Instant now = clock.instant();
        int maxAttempts = loginLockoutProperties.failedAttempts();
        Duration lockDuration = loginLockoutProperties.duration();
        loginAttemptCache.asMap().compute(email, (key, current) -> {
            LoginAttemptState incremented = (current != null ? current : new LoginAttemptState(0, null)).withIncrement();
            int attempts = incremented.failedAttempts;
            log.warn("Failed login attempt {} of {} for user {} from IP: {}",
                attempts, maxAttempts, email, clientIp);
            if (attempts >= maxAttempts) {
                log.warn("Account locked due to {} failed attempts: {}", attempts, email);
                return incremented.withLock(now.plus(lockDuration));
            }
            return incremented;
        });
    }

    private boolean isAccountLocked(String email) {
        Instant now = clock.instant();
        LoginAttemptState state = loginAttemptCache.getIfPresent(email);
        if (state == null) {
            return false;
        }
        if (state.isLocked(now)) {
            return true;
        }
        if (state.lockedUntil != null && now.isAfter(state.lockedUntil)) {
            loginAttemptCache.invalidate(email);
            log.info("Account lock expired for user: {}", email);
        }
        return false;
    }

    private void clearFailedLoginAttempts(String email) {
        loginAttemptCache.invalidate(email);
    }

    public boolean unlockAccountLockout(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return false;
        }
        return loginAttemptCache.asMap().remove(normalizedEmail) != null;
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

    @Transactional
    public void revokeRefreshTokensForUser(UUID userId) {
        revokeAllUserTokens(userId);
    }

    private void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.findAllValidByUserId(userId)
            .forEach(token -> {
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
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            if (request != null && clientIpResolver != null) {
                return clientIpResolver.resolve(request);
            }
        } catch (IllegalStateException e) {
            log.debug("No request context available for IP resolution");
        }
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

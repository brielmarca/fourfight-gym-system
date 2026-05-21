package com.gym.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.LoginRequest;
import com.gym.dto.request.RegisterRequest;
import com.gym.dto.response.TokenPairResponse;
import com.gym.dto.response.UserResponse;
import com.gym.entity.RefreshToken;
import com.gym.entity.User.Role;
import com.gym.entity.User;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.exception.UnauthorizedException;
import com.gym.repository.RefreshTokenRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${security.lockout.failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lockout.duration:PT15M}")
    private String lockoutDuration;

    private static final Map<String, Integer> failedLoginAttempts = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> lockedAccounts = new java.util.concurrent.ConcurrentHashMap<>();

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        User user = User.builder()
            .name(request.name())
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .phone(request.phone())
            .dateOfBirth(request.dateOfBirth())
            .role(User.Role.CLIENT)
            .isActive(true)
            .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return UserResponse.from(user);
    }

    @Transactional
    public TokenPairResponse login(LoginRequest request) {
        String clientIp = getClientIp();
        
        if (isAccountLocked(request.email())) {
            log.warn("Login attempt on locked account: {} from IP: {}", request.email(), clientIp);
            throw new UnauthorizedException("Account temporarily locked due to too many failed attempts");
        }

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(UnauthorizedException::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(request.email(), clientIp);
            throw UnauthorizedException.invalidCredentials();
        }

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is disabled");
        }

        clearFailedLoginAttempts(request.email());
        
        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            return generatePreAuthToken(user);
        }
        
        return generateTokenPair(user);
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
}

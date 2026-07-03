package com.gym.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.MfaSetupRequest;
import com.gym.dto.request.MfaValidateRequest;
import com.gym.dto.request.MfaVerifyRequest;
import com.gym.dto.response.MfaSetupResponse;
import com.gym.dto.response.TokenPairResponse;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.UnauthorizedException;
import com.gym.security.JwtUtil;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.AuthService;
import com.gym.service.MfaService;
import com.gym.service.RefreshTokenCookieService;
import com.gym.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;
    private final AuthService authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/setup")
    public ResponseEntity<MfaSetupResponse> setupMfa(
            @Valid @RequestBody MfaSetupRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        
        User user = userService.getUserById(principal.id());
        
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid password");
        }
        
        if (user.getMfaEnabled()) {
            throw new BusinessRuleException("MFA is already enabled");
        }
        
        String secret = mfaService.generateSecret();
        String qrCodeUrl = mfaService.generateQrCodeUrl(secret, user.getEmail());
        List<String> backupCodes = mfaService.generateBackupCodes(8);
        
        user.setMfaSecret(secret);
        user.setBackupCodes(mfaService.hashBackupCodes(backupCodes));
        userService.updateUser(user);
        
        log.info("MFA setup initiated for user: {}", user.getEmail());
        
        return ResponseEntity.ok(new MfaSetupResponse(
            secret,
            qrCodeUrl,
            backupCodes,
            "Save these backup codes. They will be needed if you lose access to your authenticator."
        ));
    }

    @PostMapping("/verify-setup")
    public ResponseEntity<Void> verifySetup(
            @Valid @RequestBody MfaVerifyRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        
        User user = userService.getUserById(principal.id());
        
        if (user.getMfaSecret() == null) {
            throw new BusinessRuleException("MFA setup not initiated. Call /setup first.");
        }
        
        if (!mfaService.verifyCode(user.getMfaSecret(), request.code())) {
            log.warn("Invalid TOTP code during MFA setup for user: {}", user.getEmail());
            throw new UnauthorizedException("Invalid verification code");
        }
        
        user.setMfaEnabled(true);
        userService.updateUser(user);
        
        log.info("MFA enabled for user: {}", user.getEmail());
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disableMfa(
            @Valid @RequestBody MfaSetupRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        
        User user = userService.getUserById(principal.id());
        
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid password");
        }
        
        if (!user.getMfaEnabled()) {
            throw new BusinessRuleException("MFA is not enabled");
        }
        
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setBackupCodes(null);
        userService.updateUser(user);
        
        log.info("MFA disabled for user: {}", user.getEmail());
        
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenPairResponse> validateMfa(
            @Valid @RequestBody MfaValidateRequest request,
            HttpServletResponse response) {
        
        if (!jwtUtil.validatePreAuthToken(request.preAuthToken())) {
            throw new UnauthorizedException("Invalid or expired pre-authentication token");
        }
        jwtUtil.extractUserId(request.preAuthToken());
        String email = jwtUtil.extractEmail(request.preAuthToken());
        
        User user = userService.getUserByEmail(email);
        
        if (!user.getMfaEnabled()) {
            throw new BusinessRuleException("MFA is not enabled for this user");
        }
        
        boolean valid = mfaService.verifyCode(user.getMfaSecret(), request.code());
        
        if (!valid && user.getBackupCodes() != null) {
            valid = mfaService.verifyBackupCode(user.getBackupCodes(), request.code());
            if (valid) {
                user.setBackupCodes(mfaService.removeUsedBackupCode(user.getBackupCodes(), request.code()));
                userService.updateUser(user);
                log.info("Backup code used for user: {}", user.getEmail());
            }
        }
        
        if (!valid) {
            throw new UnauthorizedException("Invalid MFA code");
        }
        
        TokenPairResponse tokens = authService.generateTokensForUser(user);
        refreshTokenCookieService.addRefreshTokenCookie(response, tokens.refreshToken());
        
        log.info("MFA validation successful for user: {}", user.getEmail());
        
        TokenPairResponse result = TokenPairResponse.of(tokens.accessToken(), null, tokens.expiresIn());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getMfaStatus(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        
        User user = userService.getUserById(principal.id());
        
        return ResponseEntity.ok(Map.of("enabled", user.getMfaEnabled()));
    }
}

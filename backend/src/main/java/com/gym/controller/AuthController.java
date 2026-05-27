package com.gym.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.ForgotPasswordRequest;
import com.gym.dto.request.LoginRequest;
import com.gym.dto.request.RegisterRequest;
import com.gym.dto.request.ResetPasswordRequest;
import com.gym.dto.response.TokenPairResponse;
import com.gym.dto.response.UserResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.AuthService;
import com.gym.service.PasswordResetService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenPairResponse tokens = authService.login(request);
        addRefreshTokenCookie(response, tokens.refreshToken());
        TokenPairResponse result = TokenPairResponse.of(tokens.accessToken(), null, tokens.expiresIn());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TokenPairResponse tokens = authService.refresh(refreshToken);
        addRefreshTokenCookie(response, tokens.refreshToken());
        TokenPairResponse result = TokenPairResponse.of(tokens.accessToken(), null, tokens.expiresIn());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (principal != null) {
            authService.logout(principal.id());
        } else {
            String refreshToken = extractRefreshTokenFromCookie(request);
            authService.logoutByRefreshToken(refreshToken);
        }
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.id()));
    }

    private boolean isDevProfile() {
        if (activeProfiles == null || activeProfiles.isEmpty()) {
            return false;
        }
        String[] profiles = activeProfiles.split(",");
        for (String profile : profiles) {
            if ("dev".equals(profile.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSecureCookie() {
        return !isDevProfile();
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null) return;
        boolean secure = isSecureCookie();
        String sameSite = secure ? "None" : "Lax";
        String cookieValue = "refreshToken=" + refreshToken +
                "; HttpOnly; SameSite=" + sameSite + "; Path=/; Max-Age=604800";
        if (secure) {
            cookieValue += "; Secure";
        }
        response.addHeader("Set-Cookie", cookieValue);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        boolean secure = isSecureCookie();
        String sameSite = secure ? "None" : "Lax";
        String cookieValue = "refreshToken=; HttpOnly; SameSite=" + sameSite + "; Path=/; Max-Age=0";
        if (secure) {
            cookieValue += "; Secure";
        }
        response.addHeader("Set-Cookie", cookieValue);
    }
}

package com.gym.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import com.gym.service.RefreshTokenCookieService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenPairResponse tokens = authService.login(request);
        refreshTokenCookieService.addRefreshTokenCookie(response, tokens.refreshToken());
        TokenPairResponse result = TokenPairResponse.of(tokens.accessToken(), null, tokens.expiresIn());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = refreshTokenCookieService.extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TokenPairResponse tokens = authService.refresh(refreshToken);
        refreshTokenCookieService.addRefreshTokenCookie(response, tokens.refreshToken());
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
            String refreshToken = refreshTokenCookieService.extractRefreshTokenFromCookie(request);
            authService.logoutByRefreshToken(refreshToken);
        }
        refreshTokenCookieService.clearRefreshTokenCookie(response);
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

}

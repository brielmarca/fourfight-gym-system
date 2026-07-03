package com.gym.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenCookieService {

    private static final String COOKIE_NAME = "refreshToken";
    private static final int MAX_AGE_SECONDS = 604800;

    private final String activeProfiles;

    public RefreshTokenCookieService(@Value("${spring.profiles.active:}") String activeProfiles) {
        this.activeProfiles = activeProfiles;
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null) return;
        boolean secure = isSecureCookie();
        String sameSite = secure ? "None" : "Lax";
        String cookieValue = COOKIE_NAME + "=" + refreshToken +
                "; HttpOnly; SameSite=" + sameSite + "; Path=/; Max-Age=" + MAX_AGE_SECONDS;
        if (secure) {
            cookieValue += "; Secure";
        }
        response.addHeader("Set-Cookie", cookieValue);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        boolean secure = isSecureCookie();
        String sameSite = secure ? "None" : "Lax";
        String cookieValue = COOKIE_NAME + "=; HttpOnly; SameSite=" + sameSite + "; Path=/; Max-Age=0";
        if (secure) {
            cookieValue += "; Secure";
        }
        response.addHeader("Set-Cookie", cookieValue);
    }

    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public boolean isSecureCookie() {
        return !isDevProfile();
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
}

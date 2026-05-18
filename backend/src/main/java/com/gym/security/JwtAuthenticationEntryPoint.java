package com.gym.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.*;
import org.springframework.security.web.*;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws java.io.IOException {
        log.debug("Unauthorized request: {}", request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
            java.util.Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", "Authentication required"
            )
        ));
    }
}
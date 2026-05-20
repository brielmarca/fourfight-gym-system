package com.gym.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final RateLimitFilter rateLimitFilter;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("[STARTUP] ========== START SecurityFilterChain creation ==========");
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public auth endpoints - no authentication required
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/api/auth/mfa/validate"
                ).permitAll()
                // All other auth endpoints require authentication
                .requestMatchers("/api/auth/**").authenticated()
                // Stripe webhook (no auth header from Stripe)
                .requestMatchers(HttpMethod.POST, "/api/stripe/webhook").permitAll()
                // Public read-only data
                .requestMatchers("/api/plans/**", "/api/classes/**", "/api/programs/**").permitAll()
                // Checkout session status
                .requestMatchers("/api/checkout/**").authenticated()
                // Actuator health (public)
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                // Admin: full access
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Manager: admin and manager access
                .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                // Students
                .requestMatchers(HttpMethod.GET, "/api/students/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER", "CLIENT")
                .requestMatchers(HttpMethod.POST, "/api/students/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                .requestMatchers(HttpMethod.PUT, "/api/students/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                .requestMatchers(HttpMethod.DELETE, "/api/students/**").hasAnyRole("ADMIN", "MANAGER")
                // Student martial arts
                .requestMatchers(HttpMethod.GET, "/api/student-martial-arts/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER", "CLIENT")
                .requestMatchers(HttpMethod.POST, "/api/student-martial-arts/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                .requestMatchers(HttpMethod.DELETE, "/api/student-martial-arts/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                // Martial arts and graduations
                .requestMatchers("/api/martial-arts/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                .requestMatchers("/api/graduations/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                // Trainer
                .requestMatchers("/api/trainer/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                // User endpoints
                .requestMatchers(HttpMethod.GET, "/api/user/me").hasAnyRole("ADMIN", "MANAGER", "TRAINER", "CLIENT")
                .requestMatchers("/api/user/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                // Stripe authenticated
                .requestMatchers("/api/stripe/**").authenticated()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(content -> content.disable())
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'")
                )
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            );

        log.info("[STARTUP] END SecurityFilterChain creation");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

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
        log.info("[STARTUP] START SecurityFilterChain creation");
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/mfa/validate").permitAll()
                .requestMatchers("/api/auth/**").authenticated()
                 .requestMatchers(HttpMethod.POST, "/api/checkout").permitAll()
                 .requestMatchers("/api/checkout/**").authenticated()
                 .requestMatchers("/api/plans/**", "/api/classes/**").permitAll()
                // Admin: full access to everything
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Manager: admin and manager access
                .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                // Method-level restrictions for students
                .requestMatchers(HttpMethod.GET, "/api/students/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER", "CLIENT")
                .requestMatchers(HttpMethod.POST, "/api/students/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                .requestMatchers(HttpMethod.PUT, "/api/students/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                .requestMatchers(HttpMethod.DELETE, "/api/students/**").hasAnyRole("ADMIN", "MANAGER")
                // Method-level restrictions for student-martial-arts
                .requestMatchers(HttpMethod.GET, "/api/student-martial-arts/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER", "CLIENT")
                .requestMatchers(HttpMethod.POST, "/api/student-martial-arts/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                .requestMatchers(HttpMethod.DELETE, "/api/student-martial-arts/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                // Trainer/Instructor: can manage martial arts and graduations
                .requestMatchers("/api/martial-arts/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                .requestMatchers("/api/graduations/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                // Trainer access
                .requestMatchers("/api/trainer/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                // Student/Client: read-only own data (handled in controllers via principal)
                // Only allow GET /api/user/me for CLIENT role
                .requestMatchers(HttpMethod.GET, "/api/user/me").hasAnyRole("ADMIN", "MANAGER", "TRAINER", "CLIENT")
                .requestMatchers("/api/user/**").hasAnyRole("ADMIN", "MANAGER", "TRAINER")
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/stripe/webhook").permitAll()
                .requestMatchers("/api/stripe/**").authenticated()
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

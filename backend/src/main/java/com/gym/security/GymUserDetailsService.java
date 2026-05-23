package com.gym.security;

import com.gym.entity.User;
import com.gym.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;
import org.springframework.context.annotation.Primary;
import java.util.Locale;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class GymUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        log.info("[STARTUP] START GymUserDetailsService init");
        log.info("[STARTUP] END GymUserDetailsService init");
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new JwtUserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getRole().name(),
            user.getIsActive(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    public User loadUserEntityByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record JwtUserPrincipal(
        UUID id,
        String email,
        String password,
        String role,
        Boolean isActive,
        List<SimpleGrantedAuthority> authorities
    ) implements UserDetails {

        @Override
        public String getUsername() {
            return email;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean isEnabled() {
            return isActive;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<GrantedAuthority> getAuthorities() {
            return (List<GrantedAuthority>) (List<?>) authorities;
        }
    }
}

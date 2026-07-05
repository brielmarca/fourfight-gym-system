package com.gym.service;

import java.util.EnumSet;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.UpdateUserRequest;
import com.gym.dto.response.ManagerUserDirectoryResponse;
import com.gym.dto.response.UserResponse;
import com.gym.entity.User.Role;
import com.gym.entity.User;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_USER_DIRECTORY_PAGE_SIZE = 100;
    private static final EnumSet<Role> MANAGER_OPERATIONAL_ROLES = EnumSet.of(Role.CLIENT, Role.TRAINER, Role.PROFESSOR);
    private static final java.util.Set<String> MANAGER_DIRECTORY_SORT_FIELDS = java.util.Set.of("id", "name", "email", "phone", "avatarUrl", "role");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(boundPageSize(pageable)).map(UserResponse::from);
    }

    public Page<ManagerUserDirectoryResponse> getManagerDirectory(Pageable pageable) {
        return userRepository.findActiveByRoles(MANAGER_OPERATIONAL_ROLES, boundManagerDirectoryPageable(pageable))
            .map(ManagerUserDirectoryResponse::from);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse getById(UUID id) {
        return UserResponse.from(getUserById(id));
    }

    public Object getVisibleById(UUID id, UUID requesterId, String requesterRole) {
        if ("ADMIN".equals(requesterRole) || id.equals(requesterId)) {
            return UserResponse.from(getUserById(id));
        }
        if ("MANAGER".equals(requesterRole)) {
            return ManagerUserDirectoryResponse.from(userRepository.findActiveByIdAndRoles(id, MANAGER_OPERATIONAL_ROLES)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
        }
        throw new AccessDeniedException("User is outside the permitted directory scope");
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User by email", email));
    }

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (request.isActive() != null && !isAdmin) {
            throw new AccessDeniedException("Only administrators can change user active status");
        }

        if (request.name() != null) user.setName(request.name());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());
        if (request.isActive() != null) user.setIsActive(request.isActive());

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DuplicateResourceException("User", "email", request.email());
            }
            user.setEmail(request.email());
        }

        user = userRepository.save(user);
        log.info("User updated: {}", user.getEmail());
        return UserResponse.from(user);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void delete(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
            throw new AccessDeniedException("Cannot delete privileged user accounts");
        }

        if (!isAdmin && user.getRole() != Role.CLIENT) {
            throw new AccessDeniedException("Managers can only delete student accounts");
        }

        user.softDelete();
        userRepository.save(user);
        log.info("User soft deleted: {}", user.getEmail());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateRole(UUID id, User.Role role) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setRole(role);
        user = userRepository.save(user);
        log.info("User role updated: {} -> {}", user.getEmail(), role);
        return UserResponse.from(user);
    }

    public long countActiveClients() {
        return userRepository.countActiveClients();
    }

    private Pageable boundManagerDirectoryPageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new ValidationException(java.util.Map.of("page", "Page index must not be negative"));
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!MANAGER_DIRECTORY_SORT_FIELDS.contains(order.getProperty())) {
                throw new ValidationException(java.util.Map.of("sort", "Unsupported sort field"));
            }
        }
        return boundPageSize(pageable);
    }

    private Pageable boundPageSize(Pageable pageable) {
        if (pageable.getPageSize() <= MAX_USER_DIRECTORY_PAGE_SIZE) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), MAX_USER_DIRECTORY_PAGE_SIZE, pageable.getSort());
    }
}

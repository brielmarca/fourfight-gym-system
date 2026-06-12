package com.gym.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.dto.request.CreateMembershipRequest;
import com.gym.dto.response.MembershipResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;
import com.gym.service.MembershipService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/me")
    public ResponseEntity<MembershipResponse> getMyMembership(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(membershipService.getByUserId(principal.id()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<MembershipResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(membershipService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        MembershipResponse membership = membershipService.getById(id);
        if (!membership.userId().equals(principal.id()) && !"ADMIN".equals(principal.role())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(membership);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MembershipResponse> create(@Valid @RequestBody CreateMembershipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.create(request));
    }

    @PatchMapping("/{id}/renew")
    public ResponseEntity<MembershipResponse> renew(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        MembershipResponse membership = membershipService.getById(id);
        if (!membership.userId().equals(principal.id()) && !"ADMIN".equals(principal.role())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(membershipService.renew(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MembershipResponse> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        MembershipResponse membership = membershipService.getById(id);
        if (!membership.userId().equals(principal.id()) && !"ADMIN".equals(principal.role())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(membershipService.cancel(id));
    }
}

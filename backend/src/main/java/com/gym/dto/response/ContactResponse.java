package com.gym.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import com.gym.entity.Contact;

public record ContactResponse(
    UUID id,
    String name,
    String email,
    String phone,
    String subject,
    String message,
    Contact.ContactStatus status,
    UUID resolvedBy,
    String resolvedByName,
    LocalDateTime createdAt
) {
    public static ContactResponse from(Contact contact) {
        return new ContactResponse(
            contact.getId(),
            contact.getName(),
            contact.getEmail(),
            contact.getPhone(),
            contact.getSubject(),
            contact.getMessage(),
            contact.getStatus(),
            contact.getResolvedBy() != null ? contact.getResolvedBy().getId() : null,
            contact.getResolvedBy() != null ? contact.getResolvedBy().getName() : null,
            contact.getCreatedAt()
        );
    }
}